package huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearResp {
    private java.util.UUID id;
    private String code;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private java.util.UUID schoolId;
}
