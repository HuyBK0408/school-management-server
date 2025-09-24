package huy.example.demoMonday.dto.request;

import huy.example.demoMonday.enums.RelationType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ParentRegisterReq {
    @NotBlank private String username;
    @Email @NotBlank private String email;
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,64}$",
            message = "Mật khẩu 8-64 ký tự, có hoa, thường, số, ký tự đặc biệt")
    private String newPassword;

    @NotBlank private String fullName;
    private String address;
    private String phone;
    private String photoUrl;       // optional

    /** danh sách studentCode của con */
    @NotEmpty private List<@NotBlank String> childStudentCodes;

    /** Father/Mother/Guardian... */
    @NotNull private RelationType relationType;
}
