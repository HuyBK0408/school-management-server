package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateAccountReq {
    @NotBlank private String username;
    @Email @NotBlank private String email;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "Mật khẩu 8-64 ký tự, có hoa, thường, số, ký tự đặc biệt")
    private String newPassword;
}
