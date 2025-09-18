package huy.example.demoMonday.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import huy.example.demoMonday.enums.RelationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ParentRegisterRequest(
        @NotBlank String fullName,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @NotNull RelationType relationType,
        @NotBlank String username,
        @NotBlank String password
) {}