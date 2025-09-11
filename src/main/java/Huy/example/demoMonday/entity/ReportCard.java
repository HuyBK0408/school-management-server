package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","school_year_id"}))
public class ReportCard extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    @Column(precision=4, scale=1) private java.math.BigDecimal overallAvg;
    @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.Conduct overallConduct; private String homeroomComment; private String parentComment;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public Student getStudent(){return student;} public void setStudent(Student s){this.student=s;}
    public SchoolYear getSchoolYear(){return schoolYear;} public void setSchoolYear(SchoolYear y){this.schoolYear=y;}
    public java.math.BigDecimal getOverallAvg(){return overallAvg;} public void setOverallAvg(java.math.BigDecimal a){this.overallAvg=a;}
    public Huy.example.demoMonday.enums.Conduct getOverallConduct(){return overallConduct;} public void setOverallConduct(Huy.example.demoMonday.enums.Conduct c){this.overallConduct=c;}
    public String getHomeroomComment(){return homeroomComment;} public void setHomeroomComment(String c){this.homeroomComment=c;}
    public String getParentComment(){return parentComment;} public void setParentComment(String c){this.parentComment=c;}
}