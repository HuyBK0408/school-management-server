package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","assessment_id"}))
public class ScoreEntry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @ManyToOne(optional=false) private Assessment assessment;
    @Column(precision=4, scale=1) private java.math.BigDecimal score;
    private String note;

}
