package Huy.example.demoMonday.repo;


import Huy.example.demoMonday.entity.LessonJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LessonJournalRepository extends JpaRepository<LessonJournal, UUID> {
}
