package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class StudentCardReq {
    @NotNull
    private java.util.UUID studentId;
    @NotNull private String cardNumber;
    @NotNull private java.time.LocalDate issuedDate;
    @NotNull private java.time.LocalDate expiredDate;
    @NotNull private Huy.example.demoMonday.enums.CardStatus status;
    public java.util.UUID getStudentId(){ return studentId; }
    public void setStudentId(java.util.UUID v){ this.studentId=v; }
    public String getCardNumber(){ return cardNumber; }
    public void setCardNumber(String v){ this.cardNumber=v; }
    public java.time.LocalDate getIssuedDate(){ return issuedDate; }
    public void setIssuedDate(java.time.LocalDate v){ this.issuedDate=v; }
    public java.time.LocalDate getExpiredDate(){ return expiredDate; }
    public void setExpiredDate(java.time.LocalDate v){ this.expiredDate=v; }
    public Huy.example.demoMonday.enums.CardStatus getStatus(){ return status; }
    public void setStatus(Huy.example.demoMonday.enums.CardStatus v){ this.status=v; }
}
