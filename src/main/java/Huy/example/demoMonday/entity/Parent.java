package Huy.example.demoMonday.entity;

import Huy.example.demoMonday.enums.RelationType;
import jakarta.persistence.*;

@Entity
public class Parent extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String fullName;   @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.RelationType relationType; private String phone; private String email; private String address;
    @ManyToOne private UserAccount user;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getFullName(){return fullName;} public void setFullName(String n){this.fullName=n;}
    public Huy.example.demoMonday.enums.RelationType getRelationType(){return relationType;} public void setRelationType(Huy.example.demoMonday.enums.RelationType r){this.relationType=r;}
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
    public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
    public UserAccount getUser(){return user;} public void setUser(UserAccount u){this.user=u;}
}
