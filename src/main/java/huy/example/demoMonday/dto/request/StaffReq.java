package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffReq {
    @NotNull
    private String fullName;
    @NotNull private java.time.LocalDate dob;
    @NotNull private huy.example.demoMonday.enums.Gender gender;
    @NotNull private String phone;
    @NotNull private String email;
    @NotNull private huy.example.demoMonday.enums.StaffPosition position;
    @NotNull private java.util.UUID schoolId;
    private java.util.UUID userId;

}
