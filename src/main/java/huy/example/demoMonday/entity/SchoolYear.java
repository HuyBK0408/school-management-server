package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class SchoolYear extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    private String code;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    @ManyToOne(optional = false)
    private School school;
}
