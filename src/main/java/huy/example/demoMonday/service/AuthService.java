package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.auth.*;
import huy.example.demoMonday.entity.*;
import huy.example.demoMonday.enums.Gender;

import huy.example.demoMonday.enums.SoftStatus;
import huy.example.demoMonday.repository.*;
import huy.example.demoMonday.security.JwtService;
import lombok.RequiredArgsConstructor;
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

    // ====== LOGIN (giữ nguyên API cũ của bạn) ======
    public String login(String username, String rawPassword) {
        var user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEnabled()) throw new RuntimeException("User disabled");
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");

        var roles = userRoleRepo.findRoleCodesByUserId(user.getId());
        return jwt.generate(user.getUsername(), roles);
    }

    // ====== Helpers ======
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

    private UserAccount createUser(String username, String email, String rawPassword, String roleCode){
        if (userRepo.existsByUsernameIgnoreCase(username)) throw new RuntimeException("Username đã tồn tại");
        if (userRepo.existsByEmailIgnoreCase(email)) throw new RuntimeException("Email đã tồn tại");

        var role = roleRepo.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role không hợp lệ: "+roleCode));

        var user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEnabled(false); // bật sau khi verify email
        user.setSoftStatus(SoftStatus.ACTIVE);
        userRepo.save(user);

        var ur = new UserRole();
        ur.setUser(user);
        ur.setRole(role);
        userRoleRepo.save(ur);

        // gửi mã xác thực
        var code = randomCode(6);
        var vc = new VerificationCode();
        vc.setEmail(email);
        vc.setCode(code);
        vc.setType("SIGNUP");
        vc.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        codeRepo.save(vc);

        emailService.send(email, "[School] Verify your email", "Mã xác thực: "+code+" (hết hạn 15 phút)");
        return user;
    }

    // ====== REGISTER: STUDENT ======
    @Transactional
    public void registerStudent(StudentRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getPassword(), "STUDENT");

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

    // ====== REGISTER: TEACHER ======
    @Transactional
    public void registerTeacher(TeacherRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getPassword(), "TEACHER");

        var school = schoolRepo.findById(req.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy school"));

        var staff = staffRepo.findByEmailIgnoreCase(req.getEmail()).orElseGet(Staff::new);
        if (staff.getUser()!=null) throw new RuntimeException("Giáo viên đã có tài khoản");

        staff.setFullName(req.getFullName());
        staff.setDob(req.getDob());
        staff.setGender(req.getGender());
        staff.setPhone(req.getPhone());
        staff.setEmail(req.getEmail());
        staff.setPosition(req.getPosition()); // SUBJECT_TEACHER / HOMEROOM_TEACHER / BOTH …
        staff.setSchool(school);
        staff.setUser(user);
        staffRepo.save(staff);
    }

    // ====== REGISTER: PARENT ======
    @Transactional
    public void registerParent(ParentRegisterReq req){
        var user = createUser(req.getUsername(), req.getEmail(), req.getPassword(), "PARENT");

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
                    .orElseThrow(() -> new RuntimeException("Không thấy học sinh code: "+studentCode));
            var sp = new StudentParent();
            sp.setStudent(stu);
            sp.setParent(parent);
            studentParentRepo.save(sp);
        }
    }

    // ====== VERIFY EMAIL ======
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

    // ====== ISSUE TOKENS ======
    public Map<String,String> issueTokens(UserAccount u){
        var roles = userRoleRepo.findRoleCodesByUserId(u.getId());
        var access = jwt.generate(u.getUsername(), roles);

        var refreshPlain = UUID.randomUUID()+"."+UUID.randomUUID();
        var rt = new RefreshToken();
        rt.setUser(u);
        rt.setTokenHash(sha256(refreshPlain));
        rt.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshRepo.save(rt);

        var out = new HashMap<String,String>();
        out.put("accessToken", access);
        out.put("refreshToken", refreshPlain);
        return out;
    }

    // ====== LOGIN (username or email) -> pair tokens ======
    @Transactional
    public Map<String,String> login2(LoginReq req){
        var u = userRepo.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Sai thông tin"));
        if (!u.isEnabled()) throw new RuntimeException("Chưa xác thực email");
        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash()))
            throw new RuntimeException("Sai thông tin");
        return issueTokens(u);
    }

    // ====== REFRESH ======
    @Transactional
    public Map<String,String> refresh(String refreshPlain){
        var rt = refreshRepo.findByTokenHash(sha256(refreshPlain))
                .orElseThrow(() -> new RuntimeException("Refresh không hợp lệ"));
        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("Refresh đã thu hồi/hết hạn");
        return issueTokens(rt.getUser());
    }

    // ====== LOGOUT ======
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

    // ====== FORGOT / RESET PASSWORD ======
    @Transactional
    public void forgotPassword(ForgotPasswordReq req){
        var u = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new RuntimeException("Không thấy user"));

        var code = randomCode(6);
        var vc = new VerificationCode();
        vc.setEmail(u.getEmail());
        vc.setCode(code);
        vc.setType("RESET");
        vc.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        codeRepo.save(vc);

        emailService.send(u.getEmail(), "[School] Reset password", "Mã đặt lại mật khẩu: "+code);
    }

    @Transactional
    public void resetPassword(ResetPasswordReq req){
        var vc = codeRepo.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(req.getEmail(), "RESET")
                .orElseThrow(() -> new RuntimeException("Không thấy mã"));
        if (vc.isUsed() || vc.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("Mã đã dùng/hết hạn");
        if (!vc.getCode().equalsIgnoreCase(req.getCode()))
            throw new RuntimeException("Mã không đúng");

        var u = userRepo.findByUsernameOrEmail(req.getEmail(), req.getEmail())
                .orElseThrow(() -> new RuntimeException("Không thấy user"));
        u.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(u);

        vc.setUsed(true); codeRepo.save(vc);
    }
}
