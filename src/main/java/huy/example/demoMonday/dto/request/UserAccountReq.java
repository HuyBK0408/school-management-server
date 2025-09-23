package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccountReq {
    @NotNull
    private String username;
    @NotNull private String email;
    @NotNull private String phone;
    @NotNull private Boolean enabled;
    private java.util.UUID schoolId;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "Mật khẩu 8-64 ký tự, có hoa, thường, số, ký tự đặc biệt")
    private String newPassword;
}
