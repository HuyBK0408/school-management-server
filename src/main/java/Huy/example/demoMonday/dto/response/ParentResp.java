package huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentResp {
    private java.util.UUID id;
    private String fullName;
    private huy.example.demoMonday.enums.RelationType relationType;
    private String phone;
    private String email;
    private String address;
    private java.util.UUID userId;
}
