package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ClassRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    private String name;
    @ManyToOne(optional=false) private GradeLevel gradeLevel;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    @ManyToOne(optional=false) private School school;
    @ManyToOne private Staff homeroomTeacher;

}
