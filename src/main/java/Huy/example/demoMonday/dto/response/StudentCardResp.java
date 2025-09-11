package Huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCardResp {
    private java.util.UUID id;
    private java.util.UUID studentId;
    private String cardNumber;
    private java.time.LocalDate issuedDate;
    private java.time.LocalDate expiredDate;
    private Huy.example.demoMonday.enums.CardStatus status;
}
