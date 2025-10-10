package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferStudentReq(
        @NotNull UUID targetClassId,
        String note
) {}