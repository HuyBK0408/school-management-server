package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","assessment_id"}))
public class ScoreEntry extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @ManyToOne(optional=false) private Assessment assessment;
    @Column(precision=4, scale=1) private java.math.BigDecimal score;
    private String note;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public Student getStudent(){return student;} public void setStudent(Student s){this.student=s;}
    public Assessment getAssessment(){return assessment;} public void setAssessment(Assessment a){this.assessment=a;}
    public java.math.BigDecimal getScore(){return score;} public void setScore(java.math.BigDecimal s){this.score=s;}
    public String getNote(){return note;} public void setNote(String n){this.note=n;}
}
