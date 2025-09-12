package huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermResp {
    private java.util.UUID id;
    private String name;
    private Integer orderNo;
    private java.util.UUID schoolYearId;
}
