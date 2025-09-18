package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByUserId(UUID userId);
    Optional<Student> findByStudentCode(String studentCode);
    boolean existsByStudentCode(String studentCode);
}
