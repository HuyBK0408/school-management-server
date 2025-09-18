package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.Assessment;
import huy.example.demoMonday.enums.AssessmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    long countByClassRoom_IdAndSubject_IdAndTerm_IdAndType(
            UUID classRoomId, UUID subjectId, UUID termId, AssessmentType type);

    boolean existsByClassRoom_IdAndSubject_IdAndTerm_IdAndType(
            UUID classRoomId, UUID subjectId, UUID termId, AssessmentType type);
}
