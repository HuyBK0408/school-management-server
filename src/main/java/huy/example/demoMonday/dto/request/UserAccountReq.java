package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccountReq {
    @NotNull
    private String username;
    @NotNull private String passwordHash;
    @NotNull private String email;
    @NotNull private String phone;
    @NotNull private Boolean enabled;
    private java.util.UUID schoolId;

}
