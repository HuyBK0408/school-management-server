package Huy.example.demoMonday.repo;


import Huy.example.demoMonday.entity.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {
}