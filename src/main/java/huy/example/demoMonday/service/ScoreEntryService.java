package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.response.ScoreEntryResp;
import huy.example.demoMonday.entity.Assessment;
import huy.example.demoMonday.entity.ScoreEntry;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.enums.SoftStatus;
import huy.example.demoMonday.enums.StudentStatus;
import huy.example.demoMonday.repository.AssessmentRepository;
import huy.example.demoMonday.repository.ScoreEntryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScoreEntryService {

    private final ScoreEntryRepository scoreEntryRepository;
    private final AssessmentRepository assessmentRepository;
    private final EntityManager em;
    private final GradeAggregateService gradeAggregateService;
    private final ReportCardService reportCardService;
    private final AcademicProbationService academicProbationService; // gọi cảnh báo/đuổi

    // ===== CRUD =====
    @Transactional
    public ScoreEntryResp create(ScoreEntry body) {
        if (body.getStudent() == null || body.getStudent().getId() == null) {
            throw new IllegalArgumentException("student.id is required");
        }
        if (body.getAssessment() == null || body.getAssessment().getId() == null) {
            throw new IllegalArgumentException("assessment.id is required");
        }

        UUID studentId = body.getStudent().getId();
        UUID assessmentId = body.getAssessment().getId();

        // Chặn nhập điểm cho SV đã đuổi
        assertStudentNotExpelled(studentId);

        // 1) Load assessment thật để dùng subject/term/schoolYear
        Assessment a = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assessment not found: " + assessmentId));

        // 2) Tạo score entry
        ScoreEntry e = new ScoreEntry();
        e.setStudent(em.getReference(Student.class, studentId)); // chỉ cần FK
        e.setAssessment(a);                                     // a đầy đủ quan hệ
        e.setScore(body.getScore());
        e.setNote(body.getNote());
        scoreEntryRepository.save(e);

        // 3) Recompute GA (kỳ) & ReportCard (năm)
        UUID subjectId     = a.getSubject().getId();
        UUID termId        = a.getTerm().getId();
        UUID schoolYearId  = a.getTerm().getSchoolYear().getId();

        gradeAggregateService.recomputeForTerm(studentId, subjectId, termId);
        reportCardService.recomputeForYear(studentId, schoolYearId);

        // Đảm bảo GA đã flush trước khi probation đọc
        em.flush();
        academicProbationService.previewAndApply(studentId, termId);

        // 4) Trả về RESP bạn đang dùng
        return new ScoreEntryResp(e.getId(), studentId, assessmentId, e.getScore(), e.getNote());
    }

    @Transactional
    public ScoreEntryResp update(UUID id, BigDecimal score, String note) {
        ScoreEntry e = scoreEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ScoreEntry not found: " + id));

        UUID studentId = e.getStudent().getId();

        // Chặn sửa điểm cho SV đã đuổi
        assertStudentNotExpelled(studentId);

        if (score != null) e.setScore(score);
        if (note  != null) e.setNote(note);
        scoreEntryRepository.save(e);

        // Lấy assessment đầy đủ để recompute
        Assessment a = assessmentRepository.findById(e.getAssessment().getId())
                .orElseThrow(() -> new IllegalStateException("Assessment missing for ScoreEntry: " + id));

        UUID subjectId    = a.getSubject().getId();
        UUID termId       = a.getTerm().getId();
        UUID schoolYearId = a.getTerm().getSchoolYear().getId();

        gradeAggregateService.recomputeForTerm(studentId, subjectId, termId);
        reportCardService.recomputeForYear(studentId, schoolYearId);

        em.flush();
        academicProbationService.previewAndApply(studentId, termId);

        return new ScoreEntryResp(e.getId(), studentId, a.getId(), e.getScore(), e.getNote());
    }

    @Transactional
    public ScoreEntry updateById(UUID id, BigDecimal score, String note) {
        var se = scoreEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Score entry not found"));

        var studentId = se.getStudent().getId();

        // Chặn sửa điểm cho SV đã đuổi
        assertStudentNotExpelled(studentId);

        if (score != null) se.setScore(score);
        if (note != null)  se.setNote(note);
        var saved = scoreEntryRepository.save(se);

        // Recompute sau khi sửa
        var subjectId = saved.getAssessment().getSubject().getId();
        var termId    = saved.getAssessment().getTerm().getId();
        var yearId    = saved.getAssessment().getTerm().getSchoolYear().getId();

        gradeAggregateService.recomputeForTerm(studentId, subjectId, termId);
        reportCardService.recomputeForYear(studentId, yearId);

        em.flush();
        academicProbationService.previewAndApply(studentId, termId);

        return saved;
    }

    public Optional<ScoreEntry> getOneByStudentAssessment(UUID studentId, UUID assessmentId) {
        return scoreEntryRepository.getOneByStudentAssessment(studentId, assessmentId);
    }

    public Page<ScoreEntry> listByStudent(UUID studentId, Pageable pageable) {
        return scoreEntryRepository.listByStudent(studentId, pageable);
    }
    public Page<ScoreEntry> listByAssessment(UUID assessmentId, Pageable pageable) {
        return scoreEntryRepository.listByAssessment(assessmentId, pageable);
    }
    public Page<ScoreEntry> listBySubject(UUID subjectId, Pageable pageable) {
        return scoreEntryRepository.listBySubject(subjectId, pageable);
    }
    public Page<ScoreEntry> listByTerm(UUID termId, Pageable pageable) {
        return scoreEntryRepository.listByTerm(termId, pageable);
    }

    // =================== helpers ===================

    /** SV đã đuổi? Ưu tiên status==EXPELLED; nếu không, softStatus != ACTIVE coi như đóng băng. */
    private boolean isStudentExpelled(Student s) {
        try {
            if (s.getStatus() == StudentStatus.EXPELLED) return true;
        } catch (Exception ignore) {}
        SoftStatus soft = s.getSoftStatus();
        return soft != null && soft != SoftStatus.ACTIVE;
    }

    /** Ném lỗi nếu SV đã đuổi học. */
    private void assertStudentNotExpelled(UUID studentId) {
        Student s = em.find(Student.class, studentId);
        if (s == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found: " + studentId);
        }
        if (isStudentExpelled(s)) {
            // !!! đổi từ IllegalStateException sang ResponseStatusException(409)
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Học sinh đã bị đuổi học - không thể nhập/chỉnh điểm"
            );
        }
    }
}
