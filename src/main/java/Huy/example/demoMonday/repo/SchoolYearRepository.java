package huy.example.demoMonday.repo;


import huy.example.demoMonday.entity.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {
}