package huy.example.demoMonday.config;

import huy.example.demoMonday.entity.*;
import huy.example.demoMonday.repository.*;
import huy.example.demoMonday.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PostDbBootstrap implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final UserAccountRepository userRepo;
    private final UserRoleRepository userRoleRepo;

    private final StaffRepository staffRepo;
    private final ParentRepository parentRepo;
    private final StudentRepository studentRepo;

    private final AuthService authService;     // dùng đúng salt+hash+email-verify
    private final Environment env;

    @Override
    @Transactional
    public void run(String... args) {

        // 1) Ensure roles
        ensureRole("SYSTEM_ADMIN");
        ensureRole("SCHOOL_ADMIN");
        ensureRole("TEACHER");
        ensureRole("STUDENT");
        ensureRole("PARENT");

        // ---- NEW: đọc cấu hình điều khiển seed admin & delay mail ----
        boolean seedAdmins   = env.getProperty("app.bootstrap.seed-admins", Boolean.class, true);
        boolean seedAdmin2   = env.getProperty("app.bootstrap.seed-admin2", Boolean.class, true);
        long mailDelayMillis = env.getProperty("app.bootstrap.mail-delay-ms", Long.class, 1500L);

        // 2) Seed 2 admin từ ENV/secret (KHÔNG hardcode)
        //   - ADMIN1_USERNAME, ADMIN1_EMAIL, ADMIN1_PASSWORD
        //   - ADMIN2_USERNAME, ADMIN2_EMAIL, ADMIN2_PASSWORD
        if (seedAdmins) {
            seedAdminFromEnv("ADMIN1");

            // ---- NEW: chèn delay để tránh Mailtrap rate limit ----
            if (seedAdmin2) {
                safeSleep(mailDelayMillis);
                seedAdminFromEnv("ADMIN2");
            } else {
                log.info("[Bootstrap] seed-admin2 = false → skip ADMIN2");
            }
        } else {
            log.info("[Bootstrap] seed-admins = false → skip seeding admins");
        }

        // 3) (Tuỳ chọn) seed tài khoản cho hồ sơ (mặc định OFF để an toàn)
        boolean seedProfiles = env.getProperty("app.bootstrap.seed-profiles", Boolean.class, false);
        if (seedProfiles) {
            safeSeedAccountsForStaff();
            safeSeedAccountsForParents();
            safeSeedAccountsForStudents();
        } else {
            log.info("[Bootstrap] seed-profiles = false → skip creating login for Staff/Parent/Student");
        }
    }

    /* ============ helpers ============ */

    private Role ensureRole(String code) {
        return roleRepo.findByCode(code).orElseGet(() -> {
            var r = new Role();
            r.setId(UUID.randomUUID());
            r.setCode(code);
            roleRepo.save(r);
            log.info("[Bootstrap] created role {}", code);
            return r;
        });
    }

    /**
     * Seed 1 admin (SYSTEM_ADMIN) từ ENV:
     * PREFIX_USERNAME, PREFIX_EMAIL, PREFIX_PASSWORD
     * - Nếu user đã tồn tại (username hoặc email), chỉ ensure role + bật enabled=true.
     * - Nếu chưa, tạo qua AuthService.registerUser (đúng raw+salt + gửi mail verify), rồi set enabled=true (break-glass).
     * - KHÔNG in ra mật khẩu.
     */
    private void seedAdminFromEnv(String prefix) {
        String u = env.getProperty(prefix + "_USERNAME");
        String e = env.getProperty(prefix + "_EMAIL");
        String p = env.getProperty(prefix + "_PASSWORD");

        if (isBlank(u) || isBlank(e) || isBlank(p)) {
            log.warn("[Bootstrap] {}_* missing → skip this admin", prefix);
            return;
        }

        ensureRole("SYSTEM_ADMIN");

        Optional<UserAccount> existed = userRepo.findByUsernameOrEmail(u, e);
        UserAccount user;
        if (existed.isPresent()) {
            user = existed.get();
            if (!user.isEnabled()) {
                user.setEnabled(true);
                userRepo.save(user);
                log.info("[Bootstrap] {} exists but disabled → re-enabled", prefix);
            }
            attachRole(user, roleRepo.findByCode("SYSTEM_ADMIN").orElseThrow());
            log.info("[Bootstrap] {} ok (username={}, email={})", prefix, user.getUsername(), user.getEmail());
            return;
        }

        user = authService.registerUser(u, e, p, "SYSTEM_ADMIN"); // có gửi mail verify
        user.setEnabled(true); // break-glass: cho phép đăng nhập ngay
        // nếu có field systemManaged thì mở dòng sau:
        // user.setSystemManaged(true);
        userRepo.save(user);

        attachRole(user, roleRepo.findByCode("SYSTEM_ADMIN").orElseThrow());
        log.info("[Bootstrap] {} created (username={}, email=masked)", prefix, user.getUsername());
    }

    private void attachRole(UserAccount user, Role role) {
        if (!userRoleRepo.existsByUser_IdAndRole_Id(user.getId(), role.getId())) {
            var ur = new UserRole();
            ur.setId(UUID.randomUUID());
            ur.setUser(user);
            ur.setRole(role);
            userRoleRepo.save(ur);
        }
    }

    private static boolean isBlank(String s){
        return s == null || s.trim().isEmpty();
    }

    private static void safeSleep(long ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    /* ============ Safe seeding cho hồ sơ (tùy chọn) ============ */

    private void safeSeedAccountsForStaff() {
        Role teacherRole = roleRepo.findByCode("TEACHER").orElseThrow();
        List<Staff> staffs = staffRepo.findAll();

        int created = 0;
        for (Staff s : staffs) {
            if (s.getUser() != null) continue;
            String email = safeEmail(s.getEmail());
            if (email == null) continue;

            String username = emailToBaseUsername(email, "t");
            String password = strongTempPassword();

            UserAccount u = authService.registerUser(username, email, password, "TEACHER");
            attachRole(u, teacherRole);

            s.setUser(u);
            staffRepo.save(s);
            created++;
        }
        if (created > 0) log.info("[Bootstrap] Staff seeded login accounts: {}", created);
    }

    private void safeSeedAccountsForParents() {
        Role parentRole = roleRepo.findByCode("PARENT").orElseThrow();
        List<Parent> parents = parentRepo.findAll();

        int created = 0;
        for (Parent p : parents) {
            if (p.getUser() != null) continue;
            String email = safeEmail(p.getEmail());
            if (email == null) continue;

            String username = emailToBaseUsername(email, "p");
            String password = strongTempPassword();

            UserAccount u = authService.registerUser(username, email, password, "PARENT");
            attachRole(u, parentRole);

            p.setUser(u);
            parentRepo.save(p);
            created++;
        }
        if (created > 0) log.info("[Bootstrap] Parent seeded login accounts: {}", created);
    }

    private void safeSeedAccountsForStudents() {
        Role studentRole = roleRepo.findByCode("STUDENT").orElseThrow();
        List<Student> students = studentRepo.findAll();

        int created = 0;
        for (Student st : students) {
            if (st.getUser() != null) continue;
            String email = safeEmailFromStudent(st);
            if (email == null) continue;

            String base = (st.getStudentCode() != null && !st.getStudentCode().isBlank())
                    ? st.getStudentCode().toLowerCase()
                    : "s" + UUID.randomUUID().toString().substring(0, 8);

            String username = uniqueUsername("s." + base);
            String password = strongTempPassword();

            UserAccount u = authService.registerUser(username, email, password, "STUDENT");
            attachRole(u, studentRole);

            st.setUser(u);
            studentRepo.save(st);
            created++;
        }
        if (created > 0) log.info("[Bootstrap] Student seeded login accounts: {}", created);
    }

    /* ===== utilities ===== */

    private String safeEmail(String email) {
        if (email == null) return null;
        String e = email.trim();
        if (e.isEmpty() || !e.contains("@")) return null;
        return e;
    }

    private String safeEmailFromStudent(Student st) {
        // nếu chưa có email cho student → return null để bỏ qua
        return null;
    }

    private String emailToBaseUsername(String email, String prefix) {
        String local = email.substring(0, email.indexOf('@')).toLowerCase();
        String base = prefix + "." + local.replaceAll("[^a-z0-9._-]", "");
        return uniqueUsername(base);
    }

    private String uniqueUsername(String base) {
        String u = base.toLowerCase().replaceAll("[^a-z0-9._-]", "");
        String cand = u;
        int i = 1;
        while (userRepo.existsByUsername(cand)) {
            cand = u + i;
            i++;
        }
        return cand;
    }

    private String strongTempPassword() {
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(16);
        java.util.concurrent.ThreadLocalRandom r = java.util.concurrent.ThreadLocalRandom.current();
        for (int i = 0; i < 16; i++) sb.append(alpha.charAt(r.nextInt(alpha.length())));
        return sb.toString();
    }
}
