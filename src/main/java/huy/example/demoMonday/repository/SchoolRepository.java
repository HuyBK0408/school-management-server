package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, UUID> {
}
