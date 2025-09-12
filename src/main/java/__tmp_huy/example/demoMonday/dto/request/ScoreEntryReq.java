package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;


public record ScoreEntryReq(
        @NotNull UUID studentId,
        @NotNull UUID assessmentId,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") @Digits(integer=2, fraction=1)
        BigDecimal score,
        @Size(max=255) String note
) {}
