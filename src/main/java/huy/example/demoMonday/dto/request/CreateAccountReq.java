package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountReq {
    @NotBlank private String username;
    @Email @NotBlank private String email;
    @NotBlank private String password;
}
