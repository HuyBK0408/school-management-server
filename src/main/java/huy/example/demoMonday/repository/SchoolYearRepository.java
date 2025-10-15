package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {
    Optional<SchoolYear> findTopBySchool_IdAndStartDateBeforeOrderByStartDateDesc(UUID schoolId, LocalDate startDate);
}