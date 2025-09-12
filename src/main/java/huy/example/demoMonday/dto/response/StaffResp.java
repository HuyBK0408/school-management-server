package huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResp {
    private java.util.UUID id;
    private String fullName;
    private java.time.LocalDate dob;
    private huy.example.demoMonday.enums.Gender gender;
    private String phone;
    private String email;
    private huy.example.demoMonday.enums.StaffPosition position;
    private java.util.UUID schoolId;
    private java.util.UUID userId;
}