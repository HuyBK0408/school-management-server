package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter

@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","school_year_id"}))
public class ReportCard extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    @Column(precision=4, scale=1) private java.math.BigDecimal overallAvg;
    @Enumerated(EnumType.STRING) private huy.example.demoMonday.enums.Conduct overallConduct; private String homeroomComment; private String parentComment;

}