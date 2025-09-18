package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
}
