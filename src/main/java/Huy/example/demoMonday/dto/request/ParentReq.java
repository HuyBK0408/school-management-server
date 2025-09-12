package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentReq {
    @NotNull
    private String fullName;
    @NotNull private huy.example.demoMonday.enums.RelationType relationType;
    @NotNull private String phone;
    @NotNull private String email;
    @NotNull private String address;
    private java.util.UUID userId;

}
