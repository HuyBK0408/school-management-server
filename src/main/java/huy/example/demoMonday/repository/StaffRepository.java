package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID> {
    Optional<Staff> findByUserId(UUID userId);

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findByEmailIgnoreCase(String email);
}