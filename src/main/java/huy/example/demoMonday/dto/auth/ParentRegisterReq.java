package huy.example.demoMonday.dto.auth;

import huy.example.demoMonday.enums.RelationType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ParentRegisterReq {
    @NotBlank private String username;
    @Email @NotBlank private String email;
    @NotBlank private String password;

    @NotBlank private String fullName;
    private String address;
    private String phone;

    /** danh sách studentCode của con */
    @NotEmpty private List<@NotBlank String> childStudentCodes;

    /** Father/Mother/Guardian... */
    @NotNull private RelationType relationType;
}
