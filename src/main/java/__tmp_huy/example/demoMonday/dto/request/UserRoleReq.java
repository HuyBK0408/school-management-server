package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
public class UserRoleReq {
    @NotNull
    private java.util.UUID userId;
    @NotNull private java.util.UUID roleId;

}
