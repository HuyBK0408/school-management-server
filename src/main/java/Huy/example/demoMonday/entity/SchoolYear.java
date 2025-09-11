package Huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class SchoolYear extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String code;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    @ManyToOne(optional=false) private School school;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getCode(){return code;} public void setCode(String code){this.code=code;}
    public java.time.LocalDate getStartDate(){return startDate;} public void setStartDate(java.time.LocalDate d){this.startDate=d;}
    public java.time.LocalDate getEndDate(){return endDate;} public void setEndDate(java.time.LocalDate d){this.endDate=d;}
    public School getSchool(){return school;} public void setSchool(School s){this.school=s;}
}
