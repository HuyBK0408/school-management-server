package Huy.example.demoMonday.service;
import Huy.example.demoMonday.entity.ScoreEntry;
import Huy.example.demoMonday.dto.request.ScoreEntryReq;
import Huy.example.demoMonday.dto.response.ScoreEntryResp;
import Huy.example.demoMonday.entity.Assessment;
import Huy.example.demoMonday.entity.Student;
import Huy.example.demoMonday.repo.AssessmentRepository;
import Huy.example.demoMonday.repo.ScoreEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        // 4) Trả về RESP bạn đang dùng
        return new ScoreEntryResp(e.getId(), studentId, assessmentId, e.getScore(), e.getNote());
    }

    // (Tuỳ chọn) Nếu bạn có update score thì làm tương tự:
    @Transactional
    public ScoreEntryResp update(UUID id, BigDecimal score, String note) {
        ScoreEntry e = scoreEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ScoreEntry not found: " + id));

        if (score != null) e.setScore(score);
        if (note  != null) e.setNote(note);
        scoreEntryRepository.save(e);

        // Lấy assessment đầy đủ để recompute
        Assessment a = assessmentRepository.findById(e.getAssessment().getId())
                .orElseThrow(() -> new IllegalStateException("Assessment missing for ScoreEntry: " + id));

        UUID studentId    = e.getStudent().getId();
        UUID subjectId    = a.getSubject().getId();
        UUID termId       = a.getTerm().getId();
        UUID schoolYearId = a.getTerm().getSchoolYear().getId();

        gradeAggregateService.recomputeForTerm(studentId, subjectId, termId);
        reportCardService.recomputeForYear(studentId, schoolYearId);

        return new ScoreEntryResp(e.getId(), studentId, a.getId(), e.getScore(), e.getNote());
    }


    @Transactional
    public ScoreEntry updateById(UUID id, BigDecimal score, String note) {
        var se = scoreEntryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Score entry not found"));
        if (score != null) se.setScore(score);
        if (note != null)  se.setNote(note);
        var saved = scoreEntryRepository.save(se);

        // Recompute sau khi sửa
        var studentId = saved.getStudent().getId();
        var subjectId = saved.getAssessment().getSubject().getId();
        var termId    = saved.getAssessment().getTerm().getId();
        var yearId    = saved.getAssessment().getTerm().getSchoolYear().getId();
        gradeAggregateService.recomputeForTerm(studentId, subjectId, termId);
        reportCardService.recomputeForYear(studentId, yearId);
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


}