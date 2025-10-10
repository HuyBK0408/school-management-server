package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.ExpelStudentReq;
import huy.example.demoMonday.dto.request.GraduateStudentReq;
import huy.example.demoMonday.dto.request.TransferStudentReq;
import huy.example.demoMonday.entity.ClassRoom;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.entity.StudentClassroomHistory;
import huy.example.demoMonday.entity.StudentDeletionAudit;
import huy.example.demoMonday.entity.UserAccount;
import huy.example.demoMonday.enums.SoftStatus;
import huy.example.demoMonday.enums.StudentStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentDeletionService {

    @PersistenceContext
    private final EntityManager em;

    /* =========================================================
       TRANSFER: Chỉ đổi lớp trong cùng trường (giữ ACTIVE)
       ========================================================= */
    public void transfer(UUID studentId, TransferStudentReq req) {
        Student student = lockStudentOrThrow(studentId);

        ClassRoom toClass = findClassOrThrow(req.targetClassId());

        // Chặn chuyển khác trường
        if (student.getSchool() != null && toClass.getSchool() != null
                && !student.getSchool().getId().equals(toClass.getSchool().getId())) {
            throw new IllegalArgumentException("targetClassId must belong to the same school");
        }

        UUID fromClassId = student.getCurrentClass() != null ? student.getCurrentClass().getId() : null;
        if (fromClassId != null && fromClassId.equals(toClass.getId())) {
            // Không làm gì nếu cùng lớp (tránh audit rác)
            return;
        }

        // Ghi lịch sử đổi lớp
        StudentClassroomHistory history = new StudentClassroomHistory();
        history.setStudentId(studentId);
        history.setFromClassId(fromClassId);
        history.setToClassId(toClass.getId());
        history.setNote(req.note());
        em.persist(history);

        // Cập nhật student
        student.setCurrentClass(toClass);
        student.setStatus(StudentStatus.ACTIVE);      // vẫn ACTIVE sau khi chuyển lớp
        student.setSoftStatus(SoftStatus.ACTIVE);
        // em.merge(student); // entity đang managed, không cần merge

        // Audit
        persistAudit(studentId, "TRANSFER",
                null,
                Map.of(
                        "type", "CLASS",
                        "fromClassId", fromClassId == null ? "" : fromClassId.toString(),
                        "toClassId", toClass.getId().toString(),
                        "note", req.note() == null ? "" : req.note()
                )
        );

        em.flush();
    }

    /* =========================================================
       EXPEL: Đuổi học (khóa đăng nhập, đặt soft delete)
       ========================================================= */
    public void expel(UUID studentId, ExpelStudentReq req) {
        boolean banLogin = req.banLogin() == null || req.banLogin();

        Student student = lockStudentOrThrow(studentId);

        // Nếu đã chuyển đi nơi khác (status TRANSFERRED) thì không cho đuổi ở đây
        if (student.getStatus() == StudentStatus.TRANSFERRED) {
            throw new IllegalStateException("Student already TRANSFERRED");
        }
        // Nếu đã tốt nghiệp thì cũng không nên đuổi nữa (an toàn nghiệp vụ)
        if (student.getStatus().name().equals("GRADUATED")) {
            throw new IllegalStateException("Student already GRADUATED");
        }

        student.setStatus(StudentStatus.INACTIVE);     // có thể thay bằng EXPELLED nếu bạn bổ sung enum
        student.setSoftStatus(SoftStatus.DELETED);     // soft delete để ẩn khỏi danh sách active
        // em.merge(student);

        if (banLogin) {
            disableLoginAndRevokeTokens(student.getUser());
        }

        persistAudit(studentId, "EXPEL",
                req.reason(),
                Map.of("banLogin", String.valueOf(banLogin))
        );

        em.flush();
    }

    /* =========================================================
       GRADUATE: Tốt nghiệp (khóa đăng nhập, set status GRADUATED)
       ========================================================= */
    public void graduate(UUID studentId, GraduateStudentReq req) {
        boolean issueDiploma = req.issueDiploma() == null || req.issueDiploma();

        Student student = lockStudentOrThrow(studentId);

        if (student.getStatus() == StudentStatus.TRANSFERRED) {
            throw new IllegalStateException("Cannot graduate a TRANSFERRED student");
        }

        // --- Nếu enum của bạn đã thêm GRADUATED (khuyến nghị) ---
        student.setStatus(StudentStatus.GRADUATED);

        // --- Nếu CHƯA thêm GRADUATED, dùng tạm:
        // student.setStatus(StudentStatus.INACTIVE);

        student.setSoftStatus(SoftStatus.DELETED);   // ẩn khỏi danh sách đang học
        // em.merge(student);

        // Tốt nghiệp là trạng thái cuối → khóa đăng nhập
        disableLoginAndRevokeTokens(student.getUser());

        persistAudit(studentId, "GRADUATE",
                null,
                Map.of(
                        "issueDiploma", String.valueOf(issueDiploma),
                        "note", req.note() == null ? "" : req.note()
                )
        );

        em.flush();
    }

    /* =========================
       ======== Helpers ========
       ========================= */

    private Student lockStudentOrThrow(UUID studentId) {
        Student s = em.find(Student.class, studentId, LockModeType.PESSIMISTIC_WRITE);
        if (s == null) throw new EntityNotFoundException("Student not found: " + studentId);
        return s;
    }

    private ClassRoom findClassOrThrow(UUID classId) {
        if (classId == null) throw new IllegalArgumentException("targetClassId is required");
        ClassRoom c = em.find(ClassRoom.class, classId);
        if (c == null) throw new EntityNotFoundException("ClassRoom not found: " + classId);
        return c;
    }

    /** Vô hiệu đăng nhập & thu hồi refresh token của user liên quan */
    private void disableLoginAndRevokeTokens(UserAccount user) {
        if (user == null) return;
        user.setEnabled(false);
        // em.merge(user); // managed entity

        try {
            em.createQuery("DELETE FROM RefreshToken rt WHERE rt.user.id = :uid")
                    .setParameter("uid", user.getId())
                    .executeUpdate();
        } catch (Exception ignore) {
            // Có thể log warn nếu bạn có logger
        }
    }

    /** Ghi bảng audit với note dạng "k=v;k=v;" hoặc theo cặp key-value */
    private void persistAudit(UUID studentId, String action, String reason, Map<String, String> notePairs) {
        StudentDeletionAudit a = new StudentDeletionAudit();
        a.setStudentId(studentId);
        a.setAction(action);
        a.setReason(reason);
        a.setNote(join(notePairs)); // "k=v;k=v;"
        a.setCreatedAt(OffsetDateTime.now());
        em.persist(a);
    }

    private String join(Map<String, String> map) {
        if (map == null || map.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> sb.append(k).append('=').append(v).append(';'));
        return sb.toString();
    }
}
