package huy.example.demoMonday.repository;


import huy.example.demoMonday.dto.response.GradeAggregateResp;
import huy.example.demoMonday.entity.GradeAggregate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GradeAggregateRepository extends JpaRepository<GradeAggregate, UUID> {

    // “Chung” – đúng 1 bản ghi theo (student,subject,term)
    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.student.id = :studentId and ga.subject.id = :subjectId and ga.term.id = :termId
    """)
    Optional<GradeAggregateResp> getOneByStudentSubjectTerm(
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId,
            @Param("termId") UUID termId);

    // 4 filter đơn
    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.student.id = :studentId
        order by ga.term.id desc, ga.subject.id asc
    """)
    Page<GradeAggregateResp> listByStudent(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.subject.id = :subjectId
        order by ga.term.id desc, ga.student.id asc
    """)
    Page<GradeAggregateResp> listBySubject(@Param("subjectId") UUID subjectId, Pageable pageable);

    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.term.id = :termId
        order by ga.student.id asc, ga.subject.id asc
    """)
    Page<GradeAggregateResp> listByTerm(@Param("termId") UUID termId, Pageable pageable);

    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.term.schoolYear.id = :schoolYearId
        order by ga.student.id asc, ga.subject.id asc, ga.term.id asc
    """)
    Page<GradeAggregateResp> listBySchoolYear(@Param("schoolYearId") UUID schoolYearId, Pageable pageable);

    // cặp với schoolYear
    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.student.id = :studentId and ga.term.schoolYear.id = :schoolYearId
        order by ga.term.id asc, ga.subject.id asc
    """)
    Page<GradeAggregateResp> listByStudentSchoolYear(@Param("studentId") UUID studentId,
                                                     @Param("schoolYearId") UUID schoolYearId,
                                                     Pageable pageable);

    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.subject.id = :subjectId and ga.term.schoolYear.id = :schoolYearId
        order by ga.term.id asc, ga.student.id asc
    """)
    Page<GradeAggregateResp> listBySubjectSchoolYear(@Param("subjectId") UUID subjectId,
                                                     @Param("schoolYearId") UUID schoolYearId,
                                                     Pageable pageable);

    // 4 filter tổ chức: school, gradeLevel, classroom, teacher (qua Student/Classroom hoặc SchoolYear)
    @Query("""
        select new huy.example.demoMonday.dto.response.GradeAggregateResp(
          ga.id, ga.student.id, ga.subject.id, ga.term.id,
          ga.avgScore, ga.conduct, ga.teacherComment, ga.parentComment
        )
        from GradeAggregate ga
        where ga.term.schoolYear.school.id = :schoolId
        order by ga.student.id asc, ga.subject.id asc, ga.term.id asc
    """)
    Page<GradeAggregateResp> listBySchool(@Param("schoolId") UUID schoolId, Pageable pageable);




    // Validate: term ∈ schoolYear (khi client gửi đủ 4 tham số)
    @Query("""
        select (count(t) > 0) from Term t where t.id = :termId and t.schoolYear.id = :schoolYearId
    """)
    boolean termBelongsToSchoolYear(@Param("termId") UUID termId,
                                    @Param("schoolYearId") UUID schoolYearId);

    // Upsert finder cho service recompute
    Optional<GradeAggregate> findByStudent_IdAndSubject_IdAndTerm_Id(UUID studentId, UUID subjectId, UUID termId);
}