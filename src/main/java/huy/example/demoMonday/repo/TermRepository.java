package huy.example.demoMonday.repo;


import huy.example.demoMonday.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TermRepository extends JpaRepository<Term, UUID> {

    long countBySchoolYearId(UUID schoolYearId);

    boolean existsBySchoolYearIdAndOrderNo(UUID schoolYearId, Integer orderNo);

    boolean existsBySchoolYearIdAndName(UUID schoolYearId, String name);
}
