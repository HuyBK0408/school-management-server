package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCreateUserReq {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    /**
     * Ví dụ: SYSTEM_ADMIN / TEACHER / STUDENT / PARENT
     */
    @NotBlank
    private String roleCode;
}
