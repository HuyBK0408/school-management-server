package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.*;
import huy.example.demoMonday.entity.*;
import huy.example.demoMonday.enums.SoftStatus;
import huy.example.demoMonday.repository.*;
import huy.example.demoMonday.security.LoginRateLimiter;
import huy.example.demoMonday.security.PasswordSaltUtils;
import huy.example.demoMonday.security.ProtectedAdminGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final RoleRepository roleRepo;

    private final StudentRepository studentRepo;
    private final ParentRepository parentRepo;
    private final StaffRepository staffRepo;
    private final SchoolRepository schoolRepo;
    private final ClassRoomRepository classRoomRepo;
    private final StudentParentRepository studentParentRepo;

    private final VerificationCodeRepository codeRepo;
    private final RefreshTokenRepository refreshRepo;
    // ❌ Bỏ repository blacklist theo hash token cũ
    // private final TokenBlacklistRepository blacklistRepo;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;                          // ĐÃ nâng cấp: generateAccessToken(...)
    private final EmailService emailService;
    private final ProtectedAdminGuard protectedGuard;
    private final LoginRateLimiter loginRateLimiter;
    private final TokenBlacklistService tokenBlacklistService; // ✅ MỚI: blacklist theo jti

    // Hiện thông báo chi tiết khi login (dev/test). Production nên để false.
    @Value("${security.login.reveal-detail:false}")
    private boolean revealDetail;

    // TTL refresh token (ngày) – CHUYỂN sang prefix mới
    @Value("${spring.security.jwt.refresh-days:7}")
    private int refreshDays;

    /* ======================= Helpers ======================= */

    private static final SecureRandom RNG = new SecureRandom();

    private static String randomCode(int len){
        String alpha = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < len; i++) sb.append(alpha.charAt(r.nextInt(alpha.length())));
        return sb.toString();
    }

    private static String sha256(String s){
        try{
            var md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        }catch (Exception e){ throw new RuntimeException(e); }
    }

    private Instant now() { return Instant.now(); }

    private String detailOrGeneric(String detailed, String generic) {
        return revealDetail ? detailed : generic;
    }

    /** So khớp password legacy (raw-only) & tự nâng cấp sang salt mới nếu khớp */
    private boolean upgradeIfLegacyAndMatches(UserAccount u, String raw) {
        if (passwordEncoder.matches(raw, u.getPasswordHash())) {
            String newSalt = PasswordSaltUtils.newUserSaltBase64(32);
            u.setPasswordSalt(newSalt);
            u.setPasswordHash(passwordEncoder.encode(raw + newSalt));
            userRepo.save(u);
            return true;
        }
        return false;
    }

    /** So khớp theo salt mới (raw+salt) hoặc legacy */
    private boolean matchesWithSaltOrLegacy(UserAccount u, String raw) {
        String salt = u.getPasswordSalt();
        String hash = u.getPasswordHash();
        if (salt != null && !salt.isBlank()) {
            if (passwordEncoder.matches(raw + salt, hash)) return true;
            return upgradeIfLegacyAndMatches(u, raw);
        }
        return upgradeIfLegacyAndMatches(u, raw);
    }

    /* ======================= Core: tạo UserAccount + gửi email verify ======================= */

    private UserAccount createUser(String username, String email, String rawPassword, String roleCode){
        if (userRepo.existsByUsernameIgnoreCase(username))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username đã tồn tại");
        if (userRepo.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã tồn tại");

        var role = roleRepo.findByCode(roleCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role không hợp lệ: " + roleCode));

        var user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);

        String userSalt = PasswordSaltUtils.newUserSaltBase64(32);
        user.setPasswordSalt(userSalt);
        user.setPasswordHash(passwordEncoder.encode(rawPassword + userSalt));
        user.setEnabled(false); // bật sau verify email
        user.setSoftStatus(SoftStatus.ACTIVE);
        userRepo.save(user);

        var ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        userRoleRepo.save(ur);

        // phát mã verify
        var code = randomCode(6);
        var vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(code);
        vc.setType("SIGNUP");
        vc.setExpiresAt(now().plus(15, ChronoUnit.MINUTES));
        codeRepo.save(vc);

        emailService.send(email, "[School] Verify your email", "Mã xác thực: " + code + " (hết hạn 15 phút)");
        return user;
    }

    /** Public façade để domain service gọi khi cần tạo tài khoản cho hồ sơ sẵn có */
    @Transactional
    public UserAccount registerUser(String username, String email, String rawPassword, String roleCode) {
        return createUser(username, email, rawPassword, roleCode);
    }

    /** (Tuỳ chọn) Admin tạo user độc lập */
    @Transactional
    public UserAccount adminCreateUser(AdminCreateUserReq req) {
        return createUser(req.getUsername(), req.getEmail(), req.getPassword(), req.getRoleCode());
    }

    /* ======================= REGISTER qua /auth/register/* ======================= */

    @Transactional
    public void registerStudent(StudentRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getNewPassword(), "STUDENT");

        var school = schoolRepo.findById(req.getSchoolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy school"));

        var studentOpt = studentRepo.findByStudentCode(req.getStudentCode());
        if (studentOpt.isPresent() && studentOpt.get().getUser()!=null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Học sinh đã có tài khoản");

        var s = studentOpt.orElseGet(Student::new);
        s.setFullName(req.getFullName());
        s.setDob(req.getDob());
        s.setGender(req.getGender());
        s.setStudentCode(req.getStudentCode());
        s.setSchool(school);
        s.setPhotoUrl(req.getPhotoUrl());
        s.setStatus(huy.example.demoMonday.enums.StudentStatus.ACTIVE);
        s.setUser(user);
        if (req.getCurrentClassId()!=null){
            var cls = classRoomRepo.findById(req.getCurrentClassId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy classRoom"));
            s.setCurrentClass(cls);
        }
        studentRepo.save(s);
    }

    @Transactional
    public void registerTeacher(TeacherRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getNewPassword(), "TEACHER");

        var school = schoolRepo.findById(req.getSchoolId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy school"));

        var staff = staffRepo.findByEmailIgnoreCase(req.getEmail()).orElseGet(Staff::new);
        if (staff.getUser()!=null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Giáo viên đã có tài khoản");

        staff.setFullName(req.getFullName());
        staff.setDob(req.getDob());
        staff.setGender(req.getGender());
        staff.setPhone(req.getPhone());
        staff.setEmail(req.getEmail());
        staff.setPosition(req.getPosition());
        staff.setSchool(school);
        staff.setUser(user);
        staff.setPhotoUrl(req.getPhotoUrl());
        staffRepo.save(staff);
    }

    @Transactional
    public void registerParent(ParentRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getNewPassword(), "PARENT");

        var parent = parentRepo.findByEmailIgnoreCase(req.getEmail()).orElseGet(Parent::new);
        if (parent.getUser()!=null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Phụ huynh đã có tài khoản");

        parent.setFullName(req.getFullName());
        parent.setRelationType(req.getRelationType());
        parent.setPhone(req.getPhone());
        parent.setEmail(req.getEmail());
        parent.setAddress(req.getAddress());
        parent.setUser(user);
        parent.setPhotoUrl(req.getPhotoUrl());
        parentRepo.save(parent);

        for (String studentCode: req.getChildStudentCodes()){
            var stu = studentRepo.findByStudentCode(studentCode)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không thấy học sinh code: " + studentCode));
            var sp = new StudentParent();
            sp.setStudent(stu);
            sp.setParent(parent);
            studentParentRepo.save(sp);
        }
    }

    /* ======================= VERIFY / TOKENS / FORGOT / RESET ======================= */

    @Transactional
    public void verifyEmail(VerifyEmailReq req){
        var vc = codeRepo.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(req.getEmail(), "SIGNUP")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thấy mã"));
        if (vc.isUsed()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã đã dùng");
        if (vc.getExpiresAt().isBefore(now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã hết hạn");
        if (!vc.getCode().equalsIgnoreCase(req.getCode())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã không đúng");

        vc.setUsed(true); codeRepo.save(vc);

        var user = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không thấy user"));
        user.setEnabled(true);
        userRepo.save(user);
    }

    /** Phát hành cặp token (rotate refresh) – DÙNG JwtService.generateAccessToken(...) */
    public Map<String,String> issueTokens(UserAccount u){
        var roles = userRoleRepo.findRoleCodesByUserId(u.getId()); // ["SYSTEM_ADMIN", "TEACHER", ...]
        var access = jwt.generateAccessToken(u.getUsername(), roles);

        var refreshPlain = UUID.randomUUID() + "." + UUID.randomUUID();
        var rt = new RefreshToken();
        rt.setUser(u);
        rt.setTokenHash(sha256(refreshPlain));
        rt.setExpiresAt(now().plus(refreshDays, ChronoUnit.DAYS));
        refreshRepo.save(rt);

        var out = new HashMap<String,String>();
        out.put("accessToken", access);
        out.put("refreshToken", refreshPlain);
        return out;
    }

    /* ======================= LOGIN v2 ======================= */

    @Transactional
    public Map<String, String> login2(LoginReq req) {
        String userInput = req.getUsernameOrEmail();
        String ip = huy.example.demoMonday.security.IpUtil.clientIp();

        // 1) Rate-limit: đang bị khoá
        if (loginRateLimiter.isLocked(userInput, ip)) {
            var until = loginRateLimiter.lockedUntil(userInput, ip);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    detailOrGeneric(
                            "Tài khoản/IP tạm khoá do nhập sai nhiều lần. Hết khoá lúc: " + until,
                            "Tài khoản/IP tạm khoá do nhập sai nhiều lần"
                    )
            );
        }

        // 2) Tìm user (không lộ thông tin khi production)
        var u = userRepo.findByUsernameOrEmail(userInput, userInput)
                .orElseThrow(() -> {
                    loginRateLimiter.recordFailure(userInput, ip);
                    return new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            detailOrGeneric("USER_NOT_FOUND", "Sai thông tin")
                    );
                });

        // 3) Chưa xác thực email
        if (!u.isEnabled()) {
            // KHÔNG tăng fail để tránh khoá oan
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "EMAIL_UNVERIFIED"
            );
        }

        // 4) Sai mật khẩu
        if (!matchesWithSaltOrLegacy(u, req.getPassword())) {
            loginRateLimiter.recordFailure(userInput, ip);
            int remain = loginRateLimiter.remainingAttempts(userInput, ip);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    detailOrGeneric("Mật khẩu không đúng (còn " + remain + " lần thử)", "Sai thông tin")
            );
        }

        // 5) Thành công
        loginRateLimiter.reset(userInput, ip);
        return issueTokens(u);
    }

    @Transactional
    public Map<String,String> refresh(String refreshPlain){
        var rt = refreshRepo.findByTokenHash(sha256(refreshPlain))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh không hợp lệ"));
        if (rt.isRevoked() || rt.getExpiresAt().isBefore(now()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh đã thu hồi/hết hạn");

        // rotate: revoke cái đang dùng trước khi cấp cặp mới
        rt.setRevoked(true);
        refreshRepo.save(rt);

        return issueTokens(rt.getUser());
    }

    /* ======================= LOGOUT ======================= */
    /**
     * KHÁCH HÀNG MỚI (khuyến nghị): Controller gọi với token đã decode bởi Resource Server.
     */
    @Transactional
    public void logout(Jwt jwt, String refreshPlain){
        if (jwt != null) {
            String jti = jwt.getId();
            Instant exp = jwt.getExpiresAt() != null ? jwt.getExpiresAt() : now().plus(60, ChronoUnit.SECONDS);
            tokenBlacklistService.blacklist(jti, exp); // ✅ theo jti
        }
        if (refreshPlain!=null && !refreshPlain.isBlank()){
            refreshRepo.findByTokenHash(sha256(refreshPlain)).ifPresent(rt -> {
                rt.setRevoked(true); refreshRepo.save(rt);
            });
        }
    }

    /**
     * HÀM CŨ (giữ để không vỡ compile): Controller cũ truyền access token dạng chuỗi.
     * Gợi ý chuyển sang hàm trên để blacklist theo jti chuẩn. Ở đây chỉ revoke refresh cho an toàn.
     */
    @Transactional
    public void logout(String accessToken, String refreshPlain){
        // KHÔNG dùng hash token access nữa (đã chuyển qua jti). Bỏ qua accessToken ở đây.
        if (refreshPlain!=null && !refreshPlain.isBlank()){
            refreshRepo.findByTokenHash(sha256(refreshPlain)).ifPresent(rt -> {
                rt.setRevoked(true); refreshRepo.save(rt);
            });
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordReq req){
        var u = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không thấy user"));

        if (protectedGuard.isProtected(u.getUsername(), u.getEmail()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản được bảo vệ; liên hệ quản trị.");

        var code = randomCode(6);
        var vc = new VerificationCode();
        vc.setEmail(u.getEmail());
        vc.setCode(code);
        vc.setType("RESET");
        vc.setExpiresAt(now().plus(10, ChronoUnit.MINUTES));
        codeRepo.save(vc);

        emailService.send(u.getEmail(), "[School] Reset password", "Mã đặt lại mật khẩu: " + code);
    }

    @Transactional
    public void resetPassword(ResetPasswordReq req){
        var vc = codeRepo.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(req.getEmail(), "RESET")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thấy mã"));
        if (vc.isUsed() || vc.getExpiresAt().isBefore(now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã đã dùng/hết hạn");
        if (!vc.getCode().equalsIgnoreCase(req.getCode()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã không đúng");

        var u = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không thấy user"));

        if (protectedGuard.isProtected(u.getUsername(), u.getEmail()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản được bảo vệ; liên hệ quản trị.");

        String newSalt = PasswordSaltUtils.newUserSaltBase64(32);
        u.setPasswordSalt(newSalt);
        u.setPasswordHash(passwordEncoder.encode(req.getNewPassword() + newSalt));
        userRepo.save(u);

        vc.setUsed(true); codeRepo.save(vc);

        // (tuỳ chọn an toàn hơn) revoke toàn bộ refresh tokens của user sau reset
        // refreshRepo.revokeAllByUserId(u.getId());
    }

    /* ======================= Resend verify (không cần code cũ) ======================= */
    @Transactional
    public void resendVerifyEmail(String email){
        // dọn sạch các mã SIGNUP cũ của email
        codeRepo.deleteAllByEmailAndType(email, "SIGNUP");

        var code = randomCode(6);
        var vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(code);
        vc.setType("SIGNUP");
        vc.setExpiresAt(now().plus(15, ChronoUnit.MINUTES));
        codeRepo.save(vc);

        emailService.send(email, "[School] Verify your email", "Mã xác thực: " + code + " (hết hạn 15 phút)");
    }

    /* ======================= Utils ======================= */

    private static String newUserSaltBase64(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        return Base64.getEncoder().encodeToString(buf);
    }
}
