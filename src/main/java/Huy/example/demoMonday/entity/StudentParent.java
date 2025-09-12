package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","parent_id"}))
public class StudentParent extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @ManyToOne(optional=false) private Parent parent;

}
