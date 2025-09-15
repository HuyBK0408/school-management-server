package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class LessonJournal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @ManyToOne(optional=false) @JoinColumn(name="class_id") private ClassRoom classRoom;
    @ManyToOne(optional=false) private Subject subject;
    @ManyToOne(optional=false) private Staff teacher;
    private java.time.LocalDate teachDate; private Integer periodNo;
    @Column(length=2000) private String content;
    @ManyToOne private Staff transferredFromTeacher;

}
