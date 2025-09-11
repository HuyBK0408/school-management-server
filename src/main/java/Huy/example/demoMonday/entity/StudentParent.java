package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_id","parent_id"}))
public class StudentParent extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @ManyToOne(optional=false) private Parent parent;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public Student getStudent(){return student;} public void setStudent(Student s){this.student=s;}
    public Parent getParent(){return parent;} public void setParent(Parent p){this.parent=p;}
}
