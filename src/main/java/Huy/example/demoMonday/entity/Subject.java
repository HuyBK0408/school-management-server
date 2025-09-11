package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
public class Subject extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name; @Column(unique=true) private String code;
    @ManyToOne(optional=false) private School school;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getName(){return name;} public void setName(String n){this.name=n;}
    public String getCode(){return code;} public void setCode(String c){this.code=c;}
    public School getSchool(){return school;} public void setSchool(School s){this.school=s;}
}
