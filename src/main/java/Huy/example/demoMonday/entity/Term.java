package huy.example.demoMonday.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Term extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    private String name; private Integer orderNo;
    @ManyToOne(optional=false) private SchoolYear schoolYear;

}
