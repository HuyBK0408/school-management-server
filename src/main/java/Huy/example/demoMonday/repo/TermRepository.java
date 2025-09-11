package Huy.example.demoMonday.repo;


import Huy.example.demoMonday.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TermRepository extends JpaRepository<Term, UUID> {
}
