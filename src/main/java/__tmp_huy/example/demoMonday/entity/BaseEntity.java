package huy.example.demoMonday.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@MappedSuperclass
public abstract class BaseEntity {
    @Column(name="created_at", nullable=false, updatable=false) protected java.time.Instant createdAt;
    @Column(name="updated_at", nullable=false) protected java.time.Instant updatedAt;
    @PrePersist
    public void prePersist(){ var now=java.time.Instant.now(); createdAt=now; updatedAt=now; }
    @PreUpdate
    public void preUpdate(){ updatedAt=java.time.Instant.now(); }
    public java.time.Instant getCreatedAt(){return createdAt;} public java.time.Instant getUpdatedAt(){return updatedAt;}
}
