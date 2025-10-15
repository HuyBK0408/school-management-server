package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentParentRepository extends JpaRepository<StudentParent, UUID> {
    boolean existsByStudentIdAndParentId(UUID studentId, UUID parentId);
    List<StudentParent> findByStudentId(UUID studentId);

}

