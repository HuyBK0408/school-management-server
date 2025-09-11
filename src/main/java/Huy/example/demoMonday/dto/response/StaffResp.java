package Huy.example.demoMonday.dto.response;

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
    private Huy.example.demoMonday.enums.Gender gender;
    private String phone;
    private String email;
    private Huy.example.demoMonday.enums.StaffPosition position;
    private java.util.UUID schoolId;
    private java.util.UUID userId;
}