package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "student_deletion_audit")
@Getter
@Setter
public class StudentDeletionAudit {
    @Id
    @Column(nullable=false, updatable=false)
    private UUID id;

    @Column(name="student_id", nullable=false)
    private UUID studentId;

    @Column(nullable=false, length=32)
    private String action; // EXPEL / TRANSFER / GRADUATE

    @Column(columnDefinition="text")
    private String reason;

    @Column(columnDefinition="text")
    private String note;   // "k=v;k=v;" đơn giản cho nhẹ

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    void pre(){ if(id==null) id=UUID.randomUUID(); if(createdAt==null) createdAt=OffsetDateTime.now(); }
}