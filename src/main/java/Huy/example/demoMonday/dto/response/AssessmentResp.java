package Huy.example.demoMonday.dto.response;

import Huy.example.demoMonday.enums.AssessmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResp {
    private UUID id;
    private AssessmentType assessmentType;
    private Integer weight;
    private String description;
    private UUID classId;
    private UUID subjectId;
    private UUID termId;
    private LocalDate examDate;
}
