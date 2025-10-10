package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_classroom_history")
@Getter
@Setter
public class StudentClassroomHistory {
    @Id
    @Column(nullable=false, updatable=false)
    private UUID id;

    @Column(name="student_id", nullable=false)
    private UUID studentId;

    @Column(name="from_class_id")
    private UUID fromClassId;

    @Column(name="to_class_id")
    private UUID toClassId;

    @Column(name="changed_at", nullable=false)
    private OffsetDateTime changedAt;

    @Column(columnDefinition="text")
    private String note;

    @PrePersist
    void pre(){ if(id==null) id=UUID.randomUUID(); if(changedAt==null) changedAt=OffsetDateTime.now(); }
}
