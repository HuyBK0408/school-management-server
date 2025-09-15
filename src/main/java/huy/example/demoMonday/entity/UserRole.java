package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"user_id","role_id"}))
public class UserRole extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @ManyToOne(optional=false) private UserAccount user;
    @ManyToOne(optional=false) private Role role;

}
