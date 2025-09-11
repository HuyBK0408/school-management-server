package Huy.example.demoMonday.repo;


import Huy.example.demoMonday.entity.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, UUID> {
}
