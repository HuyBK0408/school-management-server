package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Subject extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name; @Column(unique=true) private String code;
    @ManyToOne(optional=false) private School school;

}
