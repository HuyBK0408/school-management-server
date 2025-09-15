package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","subject_id","term_id"}))
public class GradeAggregate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @ManyToOne(optional = false)
    private Student student;
    @ManyToOne(optional = false)
    private Subject subject;
    @ManyToOne(optional = false)
    private Term term;
    @Column(precision = 4, scale = 1)
    private java.math.BigDecimal avgScore;
    @Enumerated(EnumType.STRING)
    private huy.example.demoMonday.enums.Conduct conduct;
    private String teacherComment;
    private String parentComment;
}

