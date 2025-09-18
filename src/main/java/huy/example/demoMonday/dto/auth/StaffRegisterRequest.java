package huy.example.demoMonday.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import huy.example.demoMonday.enums.StaffPosition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StaffRegisterRequest(
        @NotBlank String fullName,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull StaffPosition position,
        @NotBlank String username,
        @NotBlank String password
) {}
