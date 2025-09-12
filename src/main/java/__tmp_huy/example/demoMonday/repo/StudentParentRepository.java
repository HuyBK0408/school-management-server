package huy.example.demoMonday.repo;


import huy.example.demoMonday.entity.StudentParent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentParentRepository extends JpaRepository<StudentParent, UUID> {
}

