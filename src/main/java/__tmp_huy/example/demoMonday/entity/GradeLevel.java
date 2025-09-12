package huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter

public class GradeLevel extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name;
    @ManyToOne(optional=false) private School school;

}
