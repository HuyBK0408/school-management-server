package Huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class GradeLevel extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name;
    @ManyToOne(optional=false) private School school;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public School getSchool(){return school;} public void setSchool(School s){this.school=s;}
}
