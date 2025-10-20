package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.response.GradeAggregateResp;
import huy.example.demoMonday.entity.GradeAggregate;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.entity.Subject;
import huy.example.demoMonday.entity.Term;
import huy.example.demoMonday.repository.GradeAggregateRepository;
import huy.example.demoMonday.repository.ScoreEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GradeAggregateService {

    private final GradeAggregateRepository gradeAggregateRepository;
    private final ScoreEntryRepository scoreEntryRepository; // vẫn giữ cho các list API
    private final EntityManager em;

    /**
     * Recompute TB môn (SV–Subject–Term) theo trọng số Assessment.weight:
     *  avg = sum(score_i * weight_i) / sum(weight_i) (chỉ tính weight > 0 & score != null)
     *  → ghi vào GradeAggregate.avgScore (upsert).
     */
    @Transactional
    public Optional<BigDecimal> recomputeForTerm(UUID studentId, UUID subjectId, UUID termId) {
        TypedQuery<Object[]> q = em.createQuery("""
            select se.score, a.weight
            from ScoreEntry se
            join se.assessment a
            where se.student.id = :studentId
              and a.subject.id  = :subjectId
              and a.term.id     = :termId
        """, Object[].class);
        q.setParameter("studentId", studentId);
        q.setParameter("subjectId", subjectId);
        q.setParameter("termId", termId);

        var rows = q.getResultList();

        BigDecimal weighted = BigDecimal.ZERO;
        int weightSum = 0;

        for (Object[] row : rows) {
            BigDecimal score = (BigDecimal) row[0];
            Integer weight   = (Integer) row[1];
            if (score == null || weight == null || weight <= 0) continue;
            weighted = weighted.add(score.multiply(BigDecimal.valueOf(weight)));
            weightSum += weight;
        }

        GradeAggregate ga = gradeAggregateRepository
                .findByStudent_IdAndSubject_IdAndTerm_Id(studentId, subjectId, termId)
                .orElseGet(() -> {
                    GradeAggregate x = new GradeAggregate();
                    Student s = new Student(); s.setId(studentId); x.setStudent(s);
                    Subject sb = new Subject(); sb.setId(subjectId); x.setSubject(sb);
                    Term t = new Term(); t.setId(termId); x.setTerm(t);
                    return x;
                });

        if (weightSum == 0) {
            ga.setAvgScore(null); // chưa đủ dữ liệu hợp lệ
            gradeAggregateRepository.save(ga);
            return Optional.empty();
        }

        BigDecimal avg = weighted.divide(BigDecimal.valueOf(weightSum), 2, RoundingMode.HALF_UP);
        ga.setAvgScore(avg);
        gradeAggregateRepository.save(ga);
        return Optional.of(avg);
    }

    // ===== GETs (giữ & mở rộng) =====
    public Optional<GradeAggregateResp> getOneByStudentSubjectTerm(UUID studentId, UUID subjectId, UUID termId){
        return gradeAggregateRepository.getOneByStudentSubjectTerm(studentId, subjectId, termId);
    }
    public Page<GradeAggregateResp> listByStudent(UUID studentId, Pageable pageable){
        return gradeAggregateRepository.listByStudent(studentId, pageable);
    }
    public Page<GradeAggregateResp> listBySubject(UUID subjectId, Pageable pageable){
        return gradeAggregateRepository.listBySubject(subjectId, pageable);
    }
    public Page<GradeAggregateResp> listByTerm(UUID termId, Pageable pageable){
        return gradeAggregateRepository.listByTerm(termId, pageable);
    }
    public Page<GradeAggregateResp> listBySchoolYear(UUID schoolYearId, Pageable pageable){
        return gradeAggregateRepository.listBySchoolYear(schoolYearId, pageable);
    }
    public Page<GradeAggregateResp> listByStudentSchoolYear(UUID studentId, UUID schoolYearId, Pageable pageable){
        return gradeAggregateRepository.listByStudentSchoolYear(studentId, schoolYearId, pageable);
    }
    public Page<GradeAggregateResp> listBySubjectSchoolYear(UUID subjectId, UUID schoolYearId, Pageable pageable){
        return gradeAggregateRepository.listBySubjectSchoolYear(subjectId, schoolYearId, pageable);
    }
    public Page<GradeAggregateResp> listBySchool(UUID schoolId, Pageable pageable){
        return gradeAggregateRepository.listBySchool(schoolId, pageable);
    }

    public boolean termBelongsToSchoolYear(UUID termId, UUID schoolYearId){
        return gradeAggregateRepository.termBelongsToSchoolYear(termId, schoolYearId);
    }

    // giữ nguyên các logic khác như updateComments(...)
    public GradeAggregateResp updateComments(UUID id,
                                             huy.example.demoMonday.enums.Conduct conduct,
                                             String teacherComment,
                                             String parentComment) {
        throw new UnsupportedOperationException("Implement same as your existing logic");
    }
}
