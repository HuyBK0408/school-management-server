package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"teacher_id","class_id","subject_id","school_year_id"}))
public class TeachingAssignment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @ManyToOne(optional=false) private Staff teacher;
    @ManyToOne(optional=false) @JoinColumn(name="class_id") private ClassRoom classRoom;
    @ManyToOne(optional=false) private Subject subject;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    private java.time.LocalDate assignedFrom; private java.time.LocalDate assignedTo;

}
