package huy.example.demoMonday.repo;


import huy.example.demoMonday.entity.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, UUID> {
    boolean existsByTeacher_IdAndClassRoom_IdAndSubject_IdAndSchoolYear_Id(
            UUID teacherId, UUID classRoomId, UUID subjectId, UUID schoolYearId);
}
