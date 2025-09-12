package huy.example.demoMonday.dto.request;

import huy.example.demoMonday.enums.AssessmentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AssessmentReq {
    @NotNull
    private AssessmentType assessmentType;
    @NotNull @Min(1) private Integer weight;
    @Size(max=255) private String description;
    @NotNull private UUID classId;
    @NotNull private UUID subjectId;
    @NotNull private UUID termId;
    @NotNull private LocalDate examDate;


}
