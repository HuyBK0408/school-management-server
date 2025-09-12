package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolReq {
    @NotNull
    private String name;
    @NotNull private String address;
    @NotNull private String phone;
    @NotNull private String email;

}
