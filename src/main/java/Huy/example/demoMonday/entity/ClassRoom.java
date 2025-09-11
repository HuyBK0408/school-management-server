package Huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ClassRoom extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name;
    @ManyToOne(optional=false) private GradeLevel gradeLevel;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    @ManyToOne(optional=false) private School school;
    @ManyToOne private Staff homeroomTeacher;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public GradeLevel getGradeLevel(){return gradeLevel;} public void setGradeLevel(GradeLevel g){this.gradeLevel=g;}
    public SchoolYear getSchoolYear(){return schoolYear;} public void setSchoolYear(SchoolYear y){this.schoolYear=y;}
    public School getSchool(){return school;} public void setSchool(School s){this.school=s;}
    public Staff getHomeroomTeacher(){return homeroomTeacher;} public void setHomeroomTeacher(Staff t){this.homeroomTeacher=t;}
}
