package huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResp {
    private java.util.UUID id;
    private String fullName;
    private java.time.LocalDate dob;
    private huy.example.demoMonday.enums.Gender gender;
    private String studentCode;
    private java.util.UUID currentClassId;
    private java.util.UUID schoolId;
    private String photoUrl;
    private huy.example.demoMonday.enums.StudentStatus status;
}
