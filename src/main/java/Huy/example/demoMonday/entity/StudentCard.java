package Huy.example.demoMonday.entity;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"card_number"}))
public class StudentCard extends BaseEntity {
    @Id
    @GeneratedValue
    private java.util.UUID id;
    @ManyToOne(optional=false) private Student student;
    @Column(name="card_number", nullable=false, unique=true) private String cardNumber;
    private java.time.LocalDate issuedDate; private java.time.LocalDate expiredDate;   @Enumerated(EnumType.STRING) private Huy.example.demoMonday.enums.CardStatus status;
    public java.util.UUID getId(){return id;} public void setId(java.util.UUID id){this.id=id;}
    public Student getStudent(){return student;} public void setStudent(Student s){this.student=s;}
    public String getCardNumber(){return cardNumber;} public void setCardNumber(String c){this.cardNumber=c;}
    public java.time.LocalDate getIssuedDate(){return issuedDate;} public void setIssuedDate(java.time.LocalDate d){this.issuedDate=d;}
    public java.time.LocalDate getExpiredDate(){return expiredDate;} public void setExpiredDate(java.time.LocalDate d){this.expiredDate=d;}
    public Huy.example.demoMonday.enums.CardStatus getStatus(){return status;} public void setStatus(Huy.example.demoMonday.enums.CardStatus s){this.status=s;}
}
