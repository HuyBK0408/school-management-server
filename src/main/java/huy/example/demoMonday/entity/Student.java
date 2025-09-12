package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Student extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String fullName; private java.time.LocalDate dob;   @Enumerated(EnumType.STRING) private huy.example.demoMonday.enums.Gender gender;
    @Column(unique=true) private String studentCode;
    @ManyToOne
    private ClassRoom currentClass;
    @ManyToOne(optional=false) private School school;
    private String photoUrl;   @Enumerated(EnumType.STRING) private huy.example.demoMonday.enums.StudentStatus status;

}
