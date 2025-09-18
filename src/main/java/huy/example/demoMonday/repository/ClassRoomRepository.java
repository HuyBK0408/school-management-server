package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, UUID> {
    boolean existsByIdAndSchoolYearIdAndHomeroomTeacherId(UUID id, UUID schoolYearId, UUID homeroomTeacherId);
}
