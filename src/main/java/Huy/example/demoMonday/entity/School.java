package Huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class School extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name; private String address; private String phone; private String email;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getAddress(){return address;} public void setAddress(String address){this.address=address;}
    public String getPhone(){return phone;} public void setPhone(String phone){this.phone=phone;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
}
