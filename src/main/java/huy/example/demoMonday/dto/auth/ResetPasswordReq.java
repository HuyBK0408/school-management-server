package huy.example.demoMonday.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordReq {
    @Email @NotBlank private String email;
    @NotBlank private String code;
    @NotBlank private String newPassword;
}
