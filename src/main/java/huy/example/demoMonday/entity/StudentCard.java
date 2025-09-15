package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"card_number"}))
public class StudentCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @Column(name="card_number", nullable=false, unique=true) private String cardNumber;
    private java.time.LocalDate issuedDate; private java.time.LocalDate expiredDate;   @Enumerated(EnumType.STRING) private huy.example.demoMonday.enums.CardStatus status;

}
