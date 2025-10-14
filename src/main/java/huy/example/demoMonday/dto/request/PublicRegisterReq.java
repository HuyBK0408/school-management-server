package huy.example.demoMonday.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record PublicRegisterReq(
        @NotNull Role role,
        @NotNull JsonNode payload
) {
    public enum Role { STUDENT, TEACHER, PARENT }
}
