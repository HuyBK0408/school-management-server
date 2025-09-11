package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
public class UserAccount extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @Column(unique=true, nullable=false) private String username;
    private String passwordHash; private String email; private String phone; private boolean enabled=true;
    @ManyToOne
    private School school;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getUsername(){return username;} public void setUsername(String username){this.username=username;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String h){this.passwordHash=h;}
    public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean enabled){this.enabled=enabled;}
    public School getSchool(){return school;} public void setSchool(School s){this.school=s;}
}
