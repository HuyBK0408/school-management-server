package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordReq {
    @Email @NotBlank private String email;
    @NotBlank private String code;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "Mật khẩu 8-64 ký tự, có hoa, thường, số, ký tự đặc biệt")
    private String newPassword;
}
