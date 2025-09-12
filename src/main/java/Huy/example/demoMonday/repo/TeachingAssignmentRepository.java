package huy.example.demoMonday.repo;


import huy.example.demoMonday.entity.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, UUID> {
}
