package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"teacher_id","class_id","subject_id","school_year_id"}))
public class TeachingAssignment extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Staff teacher;
    @ManyToOne(optional=false) @JoinColumn(name="class_id") private ClassRoom classRoom;
    @ManyToOne(optional=false) private Subject subject;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    private java.time.LocalDate assignedFrom; private java.time.LocalDate assignedTo;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public Staff getTeacher(){return teacher;} public void setTeacher(Staff t){this.teacher=t;}
    public ClassRoom getClassRoom(){return classRoom;} public void setClassRoom(ClassRoom c){this.classRoom=c;}
    public Subject getSubject(){return subject;} public void setSubject(Subject s){this.subject=s;}
    public SchoolYear getSchoolYear(){return schoolYear;} public void setSchoolYear(SchoolYear y){this.schoolYear=y;}
    public java.time.LocalDate getAssignedFrom(){return assignedFrom;} public void setAssignedFrom(java.time.LocalDate d){this.assignedFrom=d;}
    public java.time.LocalDate getAssignedTo(){return assignedTo;} public void setAssignedTo(java.time.LocalDate d){this.assignedTo=d;}
}
