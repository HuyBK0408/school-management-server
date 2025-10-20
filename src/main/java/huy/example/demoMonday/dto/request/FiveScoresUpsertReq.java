package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FiveScoresUpsertReq(
        @NotNull UUID studentId,
        @NotNull UUID classId,
        @NotNull UUID subjectId,
        @NotNull UUID termId,

        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal quiz15,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal quiz45,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal assignment,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal midterm,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal finalExam
) {}