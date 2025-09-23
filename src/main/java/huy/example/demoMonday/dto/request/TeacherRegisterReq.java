package huy.example.demoMonday.dto.request;

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
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "Mật khẩu 8-64 ký tự, có hoa, thường, số, ký tự đặc biệt")
    private String newPassword;

    @NotNull private UUID schoolId;
    @NotBlank private String fullName;
    private LocalDate dob;
    @NotNull private Gender gender;

    /** SUBJECT_TEACHER / HOMEROOM_TEACHER / BOTH / OTHER */
    @NotNull private StaffPosition position;
    private String phone;
}
