package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"user_id","role_id"}))
public class UserRole extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private UserAccount user;
    @ManyToOne(optional=false) private Role role;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public UserAccount getUser(){return user;} public void setUser(UserAccount u){this.user=u;}
    public Role getRole(){return role;} public void setRole(Role r){this.role=r;}
}
