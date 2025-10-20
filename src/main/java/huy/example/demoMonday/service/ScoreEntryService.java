package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.response.ScoreEntryResp;
import huy.example.demoMonday.entity.*;
import huy.example.demoMonday.enums.AssessmentType;
import huy.example.demoMonday.enums.SoftStatus;
import huy.example.demoMonday.enums.StudentStatus;
import huy.example.demoMonday.repository.AssessmentRepository;
import huy.example.demoMonday.repository.ClassRoomRepository;
import huy.example.demoMonday.repository.ScoreEntryRepository;
import huy.example.demoMonday.repository.SubjectRepository;
import huy.example.demoMonday.repository.TermRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreEntryService {

    private final ScoreEntryRepository scoreEntryRepository;
    private final AssessmentRepository assessmentRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final TermRepository termRepository;

    private final EntityManager em;
    private final GradeAggregateService gradeAggregateService;
    private final ReportCardService reportCardService;
    private final AcademicProbationService academicProbationService; // gọi cảnh báo/đuổi

    // ===== CRUD (một đầu điểm/lần) =====
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

    // ====== NHẬP 1 LẦN 5 ĐẦU ĐIỂM (per SV–môn–kỳ) ======
    /**
     * Nhập 5 đầu điểm một lần:
     * QUIZ_15, QUIZ_45, ASSIGNMENT, MIDTERM, FINAL.
     * - Tự đảm bảo tồn tại đủ 5 Assessment (nếu thiếu sẽ tạo với trọng số mặc định 10/15/15/30/30).
     * - Lưu 5 ScoreEntry → Tái tính GradeAggregate + ReportCard.
     */
    @Transactional
    public void upsertFive(UUID studentId,
                           UUID classId,
                           UUID subjectId,
                           UUID termId,
                           BigDecimal quiz15,
                           BigDecimal quiz45,
                           BigDecimal assignment,
                           BigDecimal midterm,
                           BigDecimal finalExam) {

        // validate entities
        Student student = em.find(Student.class, studentId);
        if (student == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        ClassRoom clazz = classRoomRepository.findById(classId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Term not found"));

        // Chặn nhập điểm cho SV bị đuổi/bị khóa
        assertStudentNotExpelled(studentId);

        // Đảm bảo 5 assessment tồn tại cho (class, subject, term)
        Map<AssessmentType, Assessment> map = ensureAssessments(clazz, subject, term);

        // Upsert 5 score entries
        upsertScore(student, map.get(AssessmentType.QUIZ_15),    quiz15);
        upsertScore(student, map.get(AssessmentType.QUIZ_45),    quiz45);
        upsertScore(student, map.get(AssessmentType.ASSIGNMENT), assignment);
        upsertScore(student, map.get(AssessmentType.MIDTERM),    midterm);
        upsertScore(student, map.get(AssessmentType.FINAL),      finalExam);

        // Tái tính TB môn → GA, rồi TB năm → ReportCard
        gradeAggregateService.recomputeForTerm(studentId, subjectId, termId);
        reportCardService.recomputeForYear(studentId, term.getSchoolYear().getId());

        em.flush();
        academicProbationService.previewAndApply(studentId, termId);
    }

    /** Bulk upsert 5 đầu điểm cho nhiều SV một lượt */
    @Transactional
    public int bulkUpsertFive(List<BulkFiveItem> items) {
        for (BulkFiveItem it : items) {
            upsertFive(it.studentId, it.classId, it.subjectId, it.termId,
                    it.quiz15, it.quiz45, it.assignment, it.midterm, it.finalExam);
        }
        return items.size();
    }

    // ====== finder/list giữ nguyên ======
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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Học sinh đã bị đuổi học - không thể nhập/chỉnh điểm"
            );
        }
    }

    private static final Map<AssessmentType, Integer> DEFAULT_WEIGHTS = Map.of(
            AssessmentType.QUIZ_15,    10,
            AssessmentType.QUIZ_45,    15,
            AssessmentType.ASSIGNMENT, 15,
            AssessmentType.MIDTERM,    30,
            AssessmentType.FINAL,      30
    );

    private Map<AssessmentType, Assessment> ensureAssessments(ClassRoom clazz, Subject subject, Term term) {
        // lấy sẵn mọi assessment đang có
        List<Assessment> exists = assessmentRepository
                .findAllByClassRoom_IdAndSubject_IdAndTerm_Id(clazz.getId(), subject.getId(), term.getId());

        Map<AssessmentType, Assessment> map = exists.stream()
                .collect(Collectors.toMap(Assessment::getType, a -> a));

        for (AssessmentType type : AssessmentType.values()) {
            if (!map.containsKey(type)) {
                Assessment a = new Assessment();
                a.setType(type);
                a.setWeight(DEFAULT_WEIGHTS.get(type));
                a.setClassRoom(clazz);
                a.setSubject(subject);
                a.setTerm(term);
                a.setDescription("Auto-created by upsertFive: " + type);
                assessmentRepository.save(a);
                map.put(type, a);
            }
        }
        return map;
    }

    private void upsertScore(Student st, Assessment asmt, BigDecimal score) {
        var old = scoreEntryRepository.getOneByStudentAssessment(st.getId(), asmt.getId());
        ScoreEntry se = old.orElseGet(ScoreEntry::new);
        se.setStudent(st);
        se.setAssessment(asmt);
        se.setScore(score);
        scoreEntryRepository.save(se);
    }

    // ====== DTO nội bộ cho bulk ======
    public record BulkFiveItem(
            UUID studentId, UUID classId, UUID subjectId, UUID termId,
            BigDecimal quiz15, BigDecimal quiz45, BigDecimal assignment, BigDecimal midterm, BigDecimal finalExam
    ) {}
}
