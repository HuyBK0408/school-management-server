package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
@Table(name="roles")
public class Role extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @Column(unique=true, nullable=false) private String code;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public String getCode(){return code;} public void setCode(String code){this.code=code;}
}
