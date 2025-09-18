package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GradeLevelRepository extends JpaRepository<GradeLevel, UUID> {
}
