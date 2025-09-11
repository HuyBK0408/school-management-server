package Huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","subject_id","term_id"}))
public class GradeAggregate extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @ManyToOne(optional=false) private Subject subject;
    @ManyToOne(optional=false) private Term term;
    @Column(precision=4, scale=1) private java.math.BigDecimal avgScore;
    @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.Conduct conduct; private String teacherComment; private String parentComment;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public Student getStudent(){return student;} public void setStudent(Student s){this.student=s;}
    public Subject getSubject(){return subject;} public void setSubject(Subject s){this.subject=s;}
    public Term getTerm(){return term;} public void setTerm(Term t){this.term=t;}
    public java.math.BigDecimal getAvgScore(){return avgScore;} public void setAvgScore(java.math.BigDecimal a){this.avgScore=a;}
    public Huy.example.demoMonday.enums.Conduct getConduct(){return conduct;} public void setConduct(Huy.example.demoMonday.enums.Conduct c){this.conduct=c;}
    public String getTeacherComment(){return teacherComment;} public void setTeacherComment(String c){this.teacherComment=c;}
    public String getParentComment(){return parentComment;} public void setParentComment(String c){this.parentComment=c;}
}
