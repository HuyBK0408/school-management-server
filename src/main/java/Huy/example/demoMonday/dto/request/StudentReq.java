package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentReq {
    @NotNull
    private String fullName;
    @NotNull private java.time.LocalDate dob;
    @NotNull private huy.example.demoMonday.enums.Gender gender;
    @NotNull private String studentCode;
    private java.util.UUID currentClassId;
    @NotNull private java.util.UUID schoolId;
    private String photoUrl;
    @NotNull private huy.example.demoMonday.enums.StudentStatus status;

}
