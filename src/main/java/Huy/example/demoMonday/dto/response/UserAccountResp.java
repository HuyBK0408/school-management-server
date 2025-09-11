package Huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountResp {
    private java.util.UUID id;
    private String username;
    private String email;
    private String phone;
    private Boolean enabled;
    private java.util.UUID schoolId;
}
