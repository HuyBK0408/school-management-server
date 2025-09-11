package Huy.example.demoMonday.repo;


import Huy.example.demoMonday.entity.ScoreEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, UUID> {

    @Query("""
       select coalesce(avg(se.score), 0)
       from ScoreEntry se
       where se.student.id = :studentId
         and se.assessment.subject.id = :subjectId
         and se.assessment.term.id = :termId
    """)
    BigDecimal avgScoreForTerm(@Param("studentId") UUID studentId,
                               @Param("subjectId") UUID subjectId,
                               @Param("termId") UUID termId);

    @Query("""
        select se from ScoreEntry se
        where se.student.id = :studentId and se.assessment.id = :assessmentId
    """)
    Optional<ScoreEntry> getOneByStudentAssessment(@Param("studentId") UUID studentId,
                                                   @Param("assessmentId") UUID assessmentId);

    @Query("""
        select se from ScoreEntry se
        where se.student.id = :studentId
        order by se.assessment.term.id desc, se.assessment.subject.id asc
    """)
    Page<ScoreEntry> listByStudent(@Param("studentId") UUID studentId, Pageable pageable);

    @Query("""
        select se from ScoreEntry se
        where se.assessment.id = :assessmentId
        order by se.student.id asc
    """)
    Page<ScoreEntry> listByAssessment(@Param("assessmentId") UUID assessmentId, Pageable pageable);

    @Query("""
        select se from ScoreEntry se
        where se.assessment.subject.id = :subjectId
        order by se.assessment.term.id desc, se.student.id asc
    """)
    Page<ScoreEntry> listBySubject(@Param("subjectId") UUID subjectId, Pageable pageable);

    @Query("""
        select se from ScoreEntry se
        where se.assessment.term.id = :termId
        order by se.assessment.subject.id asc, se.student.id asc
    """)
    Page<ScoreEntry> listByTerm(@Param("termId") UUID termId, Pageable pageable);
}