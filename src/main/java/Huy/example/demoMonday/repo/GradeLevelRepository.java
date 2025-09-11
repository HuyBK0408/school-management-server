package Huy.example.demoMonday.repo;


import Huy.example.demoMonday.entity.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GradeLevelRepository extends JpaRepository<GradeLevel, UUID> {
}
