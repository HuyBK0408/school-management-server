package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.StudentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentCardRepository extends JpaRepository<StudentCard, UUID> {
}
