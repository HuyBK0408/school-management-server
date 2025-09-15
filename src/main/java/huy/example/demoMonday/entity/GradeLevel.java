package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class GradeLevel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    private String name;
    @ManyToOne(optional=false) private School school;

}
