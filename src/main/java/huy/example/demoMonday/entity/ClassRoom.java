package huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ClassRoom extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name;
    @ManyToOne(optional=false) private GradeLevel gradeLevel;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    @ManyToOne(optional=false) private School school;
    @ManyToOne private Staff homeroomTeacher;

}
