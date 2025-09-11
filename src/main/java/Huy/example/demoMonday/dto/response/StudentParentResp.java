package Huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentParentResp {
    private java.util.UUID id;
    private java.util.UUID studentId;
    private java.util.UUID parentId;
}
