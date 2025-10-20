package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.ReportCard;
import huy.example.demoMonday.entity.SchoolYear;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.entity.Term;
import huy.example.demoMonday.repository.ReportCardRepository;
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
public class ReportCardService {

    private final ReportCardRepository reportCardRepository;
    private final EntityManager em;

    /**
     * Tính lại ReportCard của SV theo Năm học:
     *  - TB học kỳ = trung bình cộng các GradeAggregate.avgScore (môn nào có thì tính)
     *  - TB năm = (HK1 + HK2)/2
     *  - BỎ HK3 (kỳ phụ)
     */
    @Transactional
    public void recomputeForYear(UUID studentId, UUID schoolYearId) {
        // Lấy các term của năm học
        List<Term> terms = em.createQuery("""
            select t from Term t
            where t.schoolYear.id = :y
        """, Term.class).setParameter("y", schoolYearId).getResultList();

        UUID term1 = null, term2 = null;
        for (Term t : terms) {
            int ord = resolveMainSemesterOrder(t); // 1/2/0
            if (ord == 1) term1 = t.getId();
            else if (ord == 2) term2 = t.getId();
        }

        BigDecimal sem1 = (term1 == null) ? null : semesterAverage(studentId, term1);
        BigDecimal sem2 = (term2 == null) ? null : semesterAverage(studentId, term2);

        BigDecimal yearAvg = null;
        if (sem1 != null && sem2 != null) {
            yearAvg = sem1.add(sem2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else if (sem1 != null) {
            yearAvg = sem1.setScale(2, RoundingMode.HALF_UP);
        } else if (sem2 != null) {
            yearAvg = sem2.setScale(2, RoundingMode.HALF_UP);
        }

        ReportCard rc = reportCardRepository.findByStudent_IdAndSchoolYear_Id(studentId, schoolYearId)
                .orElseGet(() -> {
                    ReportCard x = new ReportCard();
                    Student s = new Student(); s.setId(studentId); x.setStudent(s);
                    SchoolYear y = new SchoolYear(); y.setId(schoolYearId); x.setSchoolYear(y);
                    return x;
                });

        // Giả định field overallAvg; nếu bạn có field khác, đổi tên setter ở đây
        rc.setOverallAvg(yearAvg == null ? BigDecimal.ZERO.setScale(2) : yearAvg);
        reportCardRepository.save(rc);
    }

    /** TB học kỳ = trung bình cộng các GradeAggregate.avgScore trong 1 term (môn có điểm) */
    private BigDecimal semesterAverage(UUID studentId, UUID termId) {
        TypedQuery<BigDecimal> q = em.createQuery("""
            select ga.avgScore
            from GradeAggregate ga
            where ga.student.id = :s
              and ga.term.id    = :t
              and ga.avgScore is not null
        """, BigDecimal.class);
        q.setParameter("s", studentId);
        q.setParameter("t", termId);
        var list = q.getResultList();
        if (list.isEmpty()) return null;

        BigDecimal sum = BigDecimal.ZERO;
        int n = 0;
        for (BigDecimal v : list) {
            if (v != null) { sum = sum.add(v); n++; }
        }
        if (n == 0) return null;
        return sum.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
    }

    /** Trả 1 nếu HK1, 2 nếu HK2, 0 nếu kỳ phụ. Nếu model có term.getOrder() thì dùng trực tiếp. */
    private int resolveMainSemesterOrder(Term term) {
        String name = (term.getName() == null ? "" : term.getName()).toLowerCase();
        if (name.contains("1")) return 1;
        if (name.contains("2")) return 2;
        return 0;
    }

    // ===== GETs 8 filter (giữ nguyên API của bạn) =====
    public Page<ReportCard> listByStudent(UUID studentId, Pageable pageable){
        return reportCardRepository.listByStudent(studentId, pageable);
    }
    public Page<ReportCard> listBySchoolYear(UUID schoolYearId, Pageable pageable){
        return reportCardRepository.listBySchoolYear(schoolYearId, pageable);
    }
    public Page<ReportCard> listBySchool(UUID schoolId, Pageable pageable){
        return reportCardRepository.listBySchool(schoolId, pageable);
    }
    public Page<ReportCard> listBySubject(UUID subjectId, Pageable pageable){
        return reportCardRepository.listBySubject(subjectId, pageable);
    }
    public Page<ReportCard> listByTerm(UUID termId, Pageable pageable){
        return reportCardRepository.listByTerm(termId, pageable);
    }
}
