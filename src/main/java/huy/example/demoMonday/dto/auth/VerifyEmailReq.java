package huy.example.demoMonday.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VerifyEmailReq {
    @Email @NotBlank private String email;
    @NotBlank private String code;
}
