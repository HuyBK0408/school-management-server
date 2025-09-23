package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.*;
import huy.example.demoMonday.entity.*;
import huy.example.demoMonday.enums.SoftStatus;
import huy.example.demoMonday.repository.*;
import huy.example.demoMonday.security.LoginRateLimiter;
import huy.example.demoMonday.security.PasswordSaltUtils;
import huy.example.demoMonday.security.ProtectedAdminGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // ✅ thêm
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
    private final TokenBlacklistRepository blacklistRepo;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;
    private final EmailService emailService;
    private final ProtectedAdminGuard protectedGuard;
    private final LoginRateLimiter loginRateLimiter;

    // ✅ đọc TTL refresh từ cấu hình thay vì hard-code 7 ngày
    @Value("${spring.security.jwt.refresh-days:7}")
    private int refreshDays;

    /* ======================= Helpers ======================= */

    private static String randomCode(int len){
        String alpha = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(len);
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for(int i=0;i<len;i++) sb.append(alpha.charAt(r.nextInt(alpha.length())));
        return sb.toString();
    }

    private static String sha256(String s){
        try{
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for(byte b: d) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch (Exception e){ throw new RuntimeException(e); }
    }

    /** So khớp password theo salt mới (raw+salt) hoặc legacy (raw-only) và tự nâng cấp nếu legacy khớp */
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
        if (userRepo.existsByUsernameIgnoreCase(username)) throw new RuntimeException("Username đã tồn tại");
        if (userRepo.existsByEmailIgnoreCase(email)) throw new RuntimeException("Email đã tồn tại");

        var role = roleRepo.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role không hợp lệ: " + roleCode));

        var user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);

        String userSalt = PasswordSaltUtils.newUserSaltBase64(32); // ~44 chars base64
        user.setPasswordSalt(userSalt);
        user.setPasswordHash(passwordEncoder.encode(rawPassword + userSalt)); // {id}encoded

        user.setEnabled(false); // bật sau verify email
        user.setSoftStatus(SoftStatus.ACTIVE);
        userRepo.save(user);

        var ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        userRoleRepo.save(ur);

        var code = randomCode(6);
        var vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(code);
        vc.setType("SIGNUP");
        vc.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy school"));

        var studentOpt = studentRepo.findByStudentCode(req.getStudentCode());
        if (studentOpt.isPresent() && studentOpt.get().getUser()!=null)
            throw new RuntimeException("Học sinh đã có tài khoản");

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
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy classRoom"));
            s.setCurrentClass(cls);
        }
        studentRepo.save(s);
    }

    @Transactional
    public void registerTeacher(TeacherRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getNewPassword(), "TEACHER");

        var school = schoolRepo.findById(req.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy school"));

        var staff = staffRepo.findByEmailIgnoreCase(req.getEmail()).orElseGet(Staff::new);
        if (staff.getUser()!=null) throw new RuntimeException("Giáo viên đã có tài khoản");

        staff.setFullName(req.getFullName());
        staff.setDob(req.getDob());
        staff.setGender(req.getGender());
        staff.setPhone(req.getPhone());
        staff.setEmail(req.getEmail());
        staff.setPosition(req.getPosition());
        staff.setSchool(school);
        staff.setUser(user);
        staffRepo.save(staff);
    }

    @Transactional
    public void registerParent(ParentRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getNewPassword(), "PARENT");

        var parent = parentRepo.findByEmailIgnoreCase(req.getEmail()).orElseGet(Parent::new);
        if (parent.getUser()!=null) throw new RuntimeException("Phụ huynh đã có tài khoản");

        parent.setFullName(req.getFullName());
        parent.setRelationType(req.getRelationType());
        parent.setPhone(req.getPhone());
        parent.setEmail(req.getEmail());
        parent.setAddress(req.getAddress());
        parent.setUser(user);
        parentRepo.save(parent);

        for(String studentCode: req.getChildStudentCodes()){
            var stu = studentRepo.findByStudentCode(studentCode)
                    .orElseThrow(() -> new RuntimeException("Không thấy học sinh code: " + studentCode));
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
                .orElseThrow(() -> new RuntimeException("Không thấy mã"));
        if (vc.isUsed()) throw new RuntimeException("Mã đã dùng");
        if (vc.getExpiresAt().isBefore(Instant.now())) throw new RuntimeException("Mã hết hạn");
        if (!vc.getCode().equalsIgnoreCase(req.getCode())) throw new RuntimeException("Mã không đúng");

        vc.setUsed(true); codeRepo.save(vc);

        var user = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new RuntimeException("Không thấy user"));
        user.setEnabled(true);
        userRepo.save(user);
    }

    // ✅ đọc thời điểm hiện tại một chỗ
    private Instant now() { return Instant.now(); }

    // ✅ phát hành cặp token; TTL refresh lấy từ cấu hình
    public Map<String,String> issueTokens(UserAccount u){
        var roles = userRoleRepo.findRoleCodesByUserId(u.getId());
        var access = jwt.generate(u.getUsername(), roles);

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

    @Transactional
    public Map<String,String> login2(LoginReq req){
        String userInput = req.getUsernameOrEmail();
        String ip = huy.example.demoMonday.security.IpUtil.clientIp();

        // ✅ Chặn nếu đang bị khoá
        if (loginRateLimiter.isLocked(userInput, ip)) {
            var until = loginRateLimiter.lockedUntil(userInput, ip);
            throw new RuntimeException("Tài khoản/IP tạm khoá do nhập sai nhiều lần. Hết khoá lúc: " + until);
        }

        // Tìm user (không để lộ thông tin tồn tại hay không)
        var opt = userRepo.findByUsernameOrEmail(userInput, userInput);
        if (opt.isEmpty()) {
            // ✅ ghi nhận thất bại kể cả khi user không tồn tại (giảm dò username)
            loginRateLimiter.recordFailure(userInput, ip);
            throw new RuntimeException("Sai thông tin");
        }

        var u = opt.get();
        if (!u.isEnabled()) {
            // có thể không tính là thất bại để tránh khoá user hợp lệ chưa verify
            throw new RuntimeException("Chưa xác thực email");
        }

        if (!matchesWithSaltOrLegacy(u, req.getPassword())) {
            // ✅ ghi nhận thất bại khi sai mật khẩu
            loginRateLimiter.recordFailure(userInput, ip);
            int remain = loginRateLimiter.remainingAttempts(userInput, ip);
            throw new RuntimeException("Sai thông tin (còn " + remain + " lần thử)");
        }

        // ✅ đăng nhập thành công -> reset đếm
        loginRateLimiter.reset(userInput, ip);

        // (tuỳ chọn) enforce single-session: revoke toàn bộ refresh cũ của user này
        // refreshRepo.revokeAllByUserId(u.getId());

        return issueTokens(u);
    }


    @Transactional
    public Map<String,String> refresh(String refreshPlain){
        var rt = refreshRepo.findByTokenHash(sha256(refreshPlain))
                .orElseThrow(() -> new RuntimeException("Refresh không hợp lệ"));
        if (rt.isRevoked() || rt.getExpiresAt().isBefore(now()))
            throw new RuntimeException("Refresh đã thu hồi/hết hạn");

        // ✅ Xoay vòng refresh: revoke cái đang dùng trước khi cấp cặp mới
        rt.setRevoked(true);
        refreshRepo.save(rt);

        return issueTokens(rt.getUser());
    }

    @Transactional
    public void logout(String accessToken, String refreshPlain){
        if (accessToken!=null && !accessToken.isBlank()){
            var bl = new TokenBlacklist();
            bl.setTokenHash(sha256(accessToken));
            blacklistRepo.save(bl);
        }
        if (refreshPlain!=null && !refreshPlain.isBlank()){
            refreshRepo.findByTokenHash(sha256(refreshPlain)).ifPresent(rt -> {
                rt.setRevoked(true); refreshRepo.save(rt);
            });
        }
    }

    @Transactional
    public void forgotPassword(ForgotPasswordReq req){
        var u = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new RuntimeException("Không thấy user"));

        if (protectedGuard.isProtected(u.getUsername(), u.getEmail()))
            throw new RuntimeException("Tài khoản được bảo vệ; liên hệ quản trị.");

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
                .orElseThrow(() -> new RuntimeException("Không thấy mã"));
        if (vc.isUsed() || vc.getExpiresAt().isBefore(now()))
            throw new RuntimeException("Mã đã dùng/hết hạn");
        if (!vc.getCode().equalsIgnoreCase(req.getCode()))
            throw new RuntimeException("Mã không đúng");

        var u = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new RuntimeException("Không thấy user"));

        if (protectedGuard.isProtected(u.getUsername(), u.getEmail()))
            throw new RuntimeException("Tài khoản được bảo vệ; liên hệ quản trị.");

        String newSalt = PasswordSaltUtils.newUserSaltBase64(32);
        u.setPasswordSalt(newSalt);
        u.setPasswordHash(passwordEncoder.encode(req.getNewPassword() + newSalt));
        userRepo.save(u);

        vc.setUsed(true); codeRepo.save(vc);

        // (tuỳ chọn an toàn hơn) revoke tất cả refresh tokens của user sau reset mật khẩu
        // refreshRepo.revokeAllByUserId(u.getId());
    }

    /* ======================= (Optional) Resend verify ======================= */

    @Transactional
    public void resendVerifyEmail(String email){
        codeRepo.findByEmailAndType(email, "SIGNUP").ifPresent(codeRepo::delete);

        var code = randomCode(6);
        var vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(code);
        vc.setType("SIGNUP");
        vc.setExpiresAt(now().plus(15, ChronoUnit.MINUTES));
        codeRepo.save(vc);

        emailService.send(email, "[School] Verify your email", "Mã xác thực: " + code + " (hết hạn 15 phút)");
    }
}
