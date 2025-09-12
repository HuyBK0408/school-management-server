package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Staff extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String fullName; private java.time.LocalDate dob;   @Enumerated(EnumType.STRING) private huy.example.demoMonday.enums.Gender gender; private String phone; private String email;
    @Enumerated(EnumType.STRING) private huy.example.demoMonday.enums.StaffPosition position;
    @ManyToOne(optional=false) private School school;
    @ManyToOne private UserAccount user;

}
