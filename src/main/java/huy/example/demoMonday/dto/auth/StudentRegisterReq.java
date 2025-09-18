package huy.example.demoMonday.dto.auth;

import huy.example.demoMonday.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class StudentRegisterReq {
    @NotBlank private String username;
    @Email @NotBlank private String email;
    @NotBlank private String password;

    @NotNull private UUID schoolId;
    @NotBlank private String fullName;
    @NotNull private LocalDate dob;
    @NotNull private Gender gender;

    @NotBlank private String studentCode;
    private UUID currentClassId;   // optional
    private String photoUrl;       // optional
}
