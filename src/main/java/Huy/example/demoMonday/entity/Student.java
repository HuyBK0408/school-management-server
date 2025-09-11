package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
public class Student extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String fullName; private java.time.LocalDate dob;   @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.Gender gender;
    @Column(unique=true) private String studentCode;
    @ManyToOne
    private ClassRoom currentClass;
    @ManyToOne(optional=false) private School school;
    private String photoUrl;   @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.StudentStatus status;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getFullName(){return fullName;} public void setFullName(String n){this.fullName=n;}
    public java.time.LocalDate getDob(){return dob;} public void setDob(java.time.LocalDate d){this.dob=d;}
    public Huy.example.demoMonday.enums.Gender getGender(){return gender;} public void setGender(Huy.example.demoMonday.enums.Gender g){this.gender=g;}
    public String getStudentCode(){return studentCode;} public void setStudentCode(String c){this.studentCode=c;}
    public ClassRoom getCurrentClass(){return currentClass;} public void setCurrentClass(ClassRoom c){this.currentClass=c;}
    public School getSchool(){return school;} public void setSchool(School s){this.school=s;}
    public String getPhotoUrl(){return photoUrl;} public void setPhotoUrl(String p){this.photoUrl=p;}
    public Huy.example.demoMonday.enums.StudentStatus getStatus(){return status;} public void setStatus(Huy.example.demoMonday.enums.StudentStatus s){this.status=s;}
}
