package Huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Term extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name; private Integer orderNo;
    @ManyToOne(optional=false) private SchoolYear schoolYear;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getName(){return name;} public void setName(String n){this.name=n;}
    public Integer getOrderNo(){return orderNo;} public void setOrderNo(Integer o){this.orderNo=o;}
    public SchoolYear getSchoolYear(){return schoolYear;} public void setSchoolYear(SchoolYear y){this.schoolYear=y;}
}
