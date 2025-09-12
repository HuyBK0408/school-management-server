package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class Parent extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String fullName;
    @Enumerated(EnumType.STRING)
    private huy.example.demoMonday.enums.RelationType relationType;
    private String phone;
    private String email;
    private String address;
    @ManyToOne
    private UserAccount user;
}
