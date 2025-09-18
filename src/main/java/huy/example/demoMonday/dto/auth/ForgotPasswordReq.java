package huy.example.demoMonday.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ForgotPasswordReq {
    @Email @NotBlank private String email;
}
