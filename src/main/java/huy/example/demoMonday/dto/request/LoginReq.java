package huy.example.demoMonday.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {
    @NotBlank private String usernameOrEmail;
    @NotBlank private String password;
}

