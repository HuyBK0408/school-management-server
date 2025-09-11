package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
public class Staff extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String fullName; private java.time.LocalDate dob;   @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.Gender gender; private String phone; private String email;
    @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.StaffPosition position;
    @ManyToOne(optional=false) private School school;
    @ManyToOne private UserAccount user;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getFullName(){return fullName;} public void setFullName(String n){this.fullName=n;}
    public java.time.LocalDate getDob(){return dob;} public void setDob(java.time.LocalDate d){this.dob=d;}
    public Huy.example.demoMonday.enums.Gender getGender(){return gender;} public void setGender(Huy.example.demoMonday.enums.Gender g){this.gender=g;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
    public Huy.example.demoMonday.enums.StaffPosition getPosition(){return position;} public void setPosition(Huy.example.demoMonday.enums.StaffPosition p){this.position=p;}
    public School getSchool(){return school;} public void setSchool(School s){this.school=s;}
    public UserAccount getUser(){return user;} public void setUser(UserAccount u){this.user=u;}
}
