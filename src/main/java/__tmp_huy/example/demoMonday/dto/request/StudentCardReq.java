package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentCardReq {
    @NotNull
    private java.util.UUID studentId;
    @NotNull private String cardNumber;
    @NotNull private java.time.LocalDate issuedDate;
    @NotNull private java.time.LocalDate expiredDate;
    @NotNull private huy.example.demoMonday.enums.CardStatus status;

}
