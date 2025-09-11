package Huy.example.demoMonday.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

public record ScoreEntryResp(
        UUID id, UUID studentId, UUID assessmentId, BigDecimal score, String note
) {}
