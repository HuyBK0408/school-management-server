package huy.example.demoMonday.config;

import huy.example.demoMonday.entity.Parent;
import huy.example.demoMonday.entity.Role;
import huy.example.demoMonday.entity.Staff;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.entity.UserAccount;
import huy.example.demoMonday.entity.UserRole;
import huy.example.demoMonday.repo.ParentRepository;
import huy.example.demoMonday.repo.RoleRepository;
import huy.example.demoMonday.repo.StaffRepository;
import huy.example.demoMonday.repo.StudentRepository;
import huy.example.demoMonday.repo.UserAccountRepository;
import huy.example.demoMonday.repo.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class PostDbBootstrap implements CommandLineRunner {

    private final RoleRepository roleRepo;
    private final UserAccountRepository userRepo;
    private final UserRoleRepository userRoleRepo;

    private final StaffRepository staffRepo;
    private final ParentRepository parentRepo;
    private final StudentRepository studentRepo;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // 1) Ensure roles
        ensureRole("SYSTEM_ADMIN");
        ensureRole("SCHOOL_ADMIN");
        ensureRole("TEACHER");
        ensureRole("STUDENT");
        ensureRole("PARENT");

        // 2) mỗi role admin chỉ 1 account
        ensureSingleAdmin("SYSTEM_ADMIN", "sysadmin", "SYSadmin@123");
        ensureSingleAdmin("SCHOOL_ADMIN","schooladmin","SchoolAdmin@123");

        // 3) seed tài khoản cho staff/parent/student chưa có user
        seedAccountsForStaff();
        seedAccountsForParents();
        seedAccountsForStudents();
    }

    /* ============ helpers ============ */

    private Role ensureRole(String code) {
        return roleRepo.findByCode(code).orElseGet(() -> {
            var r = new Role();
            r.setId(UUID.randomUUID());
            r.setCode(code);
            return roleRepo.save(r);
        });
    }

    private void ensureSingleAdmin(String roleCode, String username, String rawPassword) {
        // nếu đã có ít nhất 1 user có role này thì bỏ qua
        if (userRoleRepo.existsByRole_Code(roleCode)) return;

        Role role = roleRepo.findByCode(roleCode).orElseThrow();

        // tạo user (nếu tên đã tồn tại thì dùng lại, tránh trùng unique)
        UserAccount user = userRepo.findByUsername(username).orElseGet(() -> {
            var u = new UserAccount();
            u.setId(UUID.randomUUID());
            u.setUsername(username);
            u.setPasswordHash(passwordEncoder.encode(rawPassword));
            u.setEnabled(true);
            return userRepo.save(u);
        });

        attachRole(user, role);
        System.out.println("[Bootstrap] " + roleCode + " = " + username + " / " + rawPassword);
    }

    private void seedAccountsForStaff() {
        Role teacherRole = roleRepo.findByCode("TEACHER").orElseThrow();
        List<Staff> staffs = staffRepo.findAll();

        for (Staff s : staffs) {
            if (s.getUser() != null) continue;                 // <— Dùng object mapping

            String base = safeUsernameBase(s.getFullName(), "teacher");
            String username = uniqueUsername("t." + base);
            String rawPass  = "T@" + safeTail(s.getPhone(), 6, "123456");

            UserAccount u = createUser(username, rawPass);
            s.setUser(u);                                       // <— set user object
            staffRepo.save(s);

            attachRole(u, teacherRole);
            System.out.println("[Bootstrap] TEACHER: " + username + " / " + rawPass);
        }
    }

    private void seedAccountsForParents() {
        Role parentRole = roleRepo.findByCode("PARENT").orElseThrow();
        List<Parent> parents = parentRepo.findAll();

        for (Parent p : parents) {
            if (p.getUser() != null) continue;

            String base = safeUsernameBase(p.getFullName(), "parent");
            String username = uniqueUsername("p." + base);
            String rawPass  = "P@" + safeTail(p.getPhone(), 6, "123456");

            UserAccount u = createUser(username, rawPass);
            p.setUser(u);
            parentRepo.save(p);

            attachRole(u, parentRole);
            System.out.println("[Bootstrap] PARENT: " + username + " / " + rawPass);
        }
    }

    private void seedAccountsForStudents() {
        Role studentRole = roleRepo.findByCode("STUDENT").orElseThrow();
        List<Student> students = studentRepo.findAll();

        for (Student st : students) {
            if (st.getUser() != null) continue;

            String code = (st.getStudentCode() != null && !st.getStudentCode().isBlank())
                    ? st.getStudentCode().toLowerCase()
                    : UUID.randomUUID().toString().substring(0, 8);

            String username = uniqueUsername("s." + code);
            String rawPass  = "S@" + code.substring(0, Math.min(6, code.length()));

            UserAccount u = createUser(username, rawPass);
            st.setUser(u);
            studentRepo.save(st);

            attachRole(u, studentRole);
            System.out.println("[Bootstrap] STUDENT: " + username + " / " + rawPass);
        }
    }

    private UserAccount createUser(String username, String rawPassword) {
        var u = new UserAccount();
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setEnabled(true);
        return userRepo.save(u);
    }

    private void attachRole(UserAccount user, Role role) {
        if (!userRoleRepo.existsByUser_IdAndRole_Id(user.getId(), role.getId())) {
            var ur = new UserRole();
            ur.setId(UUID.randomUUID());
            ur.setUser(user);    // <— mapping theo object
            ur.setRole(role);
            userRoleRepo.save(ur);
        }
    }

    private static String safeUsernameBase(String name, String fallback) {
        String base = (name == null || name.isBlank()) ? fallback : name;
        return base.toLowerCase().replaceAll("[^a-z0-9]+", ".");
    }

    private static String safeTail(String s, int n, String deflt) {
        if (s == null || s.isBlank()) return deflt;
        return s.length() <= n ? s : s.substring(s.length() - n);
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
}
