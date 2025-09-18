package huy.example.demoMonday.dto.auth;

import huy.example.demoMonday.enums.Gender;
import huy.example.demoMonday.enums.StaffPosition;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class TeacherRegisterReq {
    @NotBlank private String username;
    @Email @NotBlank private String email;
    @NotBlank private String password;

    @NotNull private UUID schoolId;
    @NotBlank private String fullName;
    private LocalDate dob;
    @NotNull private Gender gender;

    /** SUBJECT_TEACHER / HOMEROOM_TEACHER / BOTH / OTHER */
    @NotNull private StaffPosition position;
    private String phone;
}
