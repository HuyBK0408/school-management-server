package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParentRepository extends JpaRepository<Parent, UUID> {
    Optional<Parent> findByUserId(UUID userId);

    Optional<Parent> findByEmail(String email);

    Optional<Parent> findByEmailIgnoreCase(String email);
}
