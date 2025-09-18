package huy.example.demoMonday.repository;

import huy.example.demoMonday.entity.ReportCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ReportCardRepository extends JpaRepository<ReportCard, UUID> {

    Optional<ReportCard> findByStudent_IdAndSchoolYear_Id(UUID studentId, UUID schoolYearId);

    @Query("""
        select coalesce(avg(ga.avgScore), 0)
        from GradeAggregate ga
        where ga.student.id = :studentId
          and ga.term.schoolYear.id = :schoolYearId
    """)
    BigDecimal overallAvgForYear(@Param("studentId") UUID studentId,
                                 @Param("schoolYearId") UUID schoolYearId);

    // 8 filter cho ReportCard (với subject/term: EXISTS GA trong năm đó khớp)
    @Query("select rc from ReportCard rc where rc.student.id = :studentId order by rc.schoolYear.id desc")
    Page<ReportCard> listByStudent(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("select rc from ReportCard rc where rc.schoolYear.id = :schoolYearId order by rc.student.id asc")
    Page<ReportCard> listBySchoolYear(@Param("schoolYearId") UUID schoolYearId, Pageable pageable);

    @Query("""
        select rc from ReportCard rc
        where rc.schoolYear.school.id = :schoolId
        order by rc.schoolYear.id desc, rc.student.id asc
    """)
    Page<ReportCard> listBySchool(@Param("schoolId") UUID schoolId, Pageable pageable);



    @Query("""
        select rc from ReportCard rc
        where exists (
            select 1 from GradeAggregate ga
            where ga.student.id = rc.student.id
              and ga.term.schoolYear.id = rc.schoolYear.id
              and ga.subject.id = :subjectId
        )
        order by rc.schoolYear.id desc, rc.student.id asc
    """)
    Page<ReportCard> listBySubject(@Param("subjectId") UUID subjectId, Pageable pageable);

    @Query("""
        select rc from ReportCard rc
        where exists (
            select 1 from GradeAggregate ga
            where ga.student.id = rc.student.id
              and ga.term.schoolYear.id = rc.schoolYear.id
              and ga.term.id = :termId
        )
        order by rc.schoolYear.id desc, rc.student.id asc
    """)
    Page<ReportCard> listByTerm(@Param("termId") UUID termId, Pageable pageable);
}