package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name="roles")
public class Role extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @Column(unique=true, nullable=false) private String code;

}
