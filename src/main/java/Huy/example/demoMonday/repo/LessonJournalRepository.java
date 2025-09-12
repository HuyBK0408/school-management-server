package huy.example.demoMonday.repo;


import huy.example.demoMonday.entity.LessonJournal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LessonJournalRepository extends JpaRepository<LessonJournal, UUID> {
}
