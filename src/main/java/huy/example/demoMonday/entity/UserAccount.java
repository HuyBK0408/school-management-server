package huy.example.demoMonday.entity;

import huy.example.demoMonday.enums.SoftStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class UserAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @Column(unique=true, nullable=false) private String username;
    private String passwordHash; private String email; private String phone; private boolean enabled=true;
    @ManyToOne
    private School school;

    @Enumerated(EnumType.STRING)
    @Column(name = "soft_status", nullable = false)
    private SoftStatus softStatus = SoftStatus.ACTIVE;


}
