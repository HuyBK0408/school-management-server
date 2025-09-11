package Huy.example.demoMonday.repo;


import Huy.example.demoMonday.entity.StudentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentCardRepository extends JpaRepository<StudentCard, UUID> {
}
