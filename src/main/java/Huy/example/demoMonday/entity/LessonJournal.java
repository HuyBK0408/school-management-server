package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
public class LessonJournal extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) @JoinColumn(name="class_id") private ClassRoom classRoom;
    @ManyToOne(optional=false) private Subject subject;
    @ManyToOne(optional=false) private Staff teacher;
    private java.time.LocalDate teachDate; private Integer periodNo;
    @Column(length=2000) private String content;
    @ManyToOne private Staff transferredFromTeacher;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public ClassRoom getClassRoom(){return classRoom;} public void setClassRoom(ClassRoom c){this.classRoom=c;}
    public Subject getSubject(){return subject;} public void setSubject(Subject s){this.subject=s;}
    public Staff getTeacher(){return teacher;} public void setTeacher(Staff t){this.teacher=t;}
    public java.time.LocalDate getTeachDate(){return teachDate;} public void setTeachDate(java.time.LocalDate d){this.teachDate=d;}
    public Integer getPeriodNo(){return periodNo;} public void setPeriodNo(Integer p){this.periodNo=p;}
    public String getContent(){return content;} public void setContent(String c){this.content=c;}
    public Staff getTransferredFromTeacher(){return transferredFromTeacher;} public void setTransferredFromTeacher(Staff t){this.transferredFromTeacher=t;}
}
