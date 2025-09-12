package huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeLevelResp {
    private java.util.UUID id;
    private String name;
    private java.util.UUID schoolId;
}
