package Huy.example.demoMonday.service;

import Huy.example.demoMonday.dto.response.ReportCardResp;
import Huy.example.demoMonday.entity.GradeAggregate;
import Huy.example.demoMonday.entity.ReportCard;
import Huy.example.demoMonday.entity.SchoolYear;
import Huy.example.demoMonday.entity.Student;
import Huy.example.demoMonday.repo.GradeAggregateRepository;
import Huy.example.demoMonday.repo.ReportCardRepository;
import jakarta.persistence.EntityManager;
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

    // ===== Recompute: trung bình THEO NĂM =====
    @Transactional
    public void recomputeForYear(UUID studentId, UUID schoolYearId) {
        BigDecimal overall = reportCardRepository.overallAvgForYear(studentId, schoolYearId);
        if (overall == null) overall = BigDecimal.ZERO;
        overall = overall.setScale(2, RoundingMode.HALF_UP);

        ReportCard rc = reportCardRepository.findByStudent_IdAndSchoolYear_Id(studentId, schoolYearId)
                .orElseGet(() -> {
                    ReportCard x = new ReportCard();
                    Student s = new Student();
                    s.setId(studentId);
                    x.setStudent(s);

                    SchoolYear y = new SchoolYear();
                    y.setId(schoolYearId);
                    x.setSchoolYear(y);
                    return x;
                });
        rc.setOverallAvg(overall);
        reportCardRepository.save(rc);
    }

    // ===== GETs 8 filter =====
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