package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeachingAssignmentReq {
    @NotNull
    private java.util.UUID teacherId;
    @NotNull private java.util.UUID classId;
    @NotNull private java.util.UUID subjectId;
    @NotNull private java.util.UUID schoolYearId;
    @NotNull private java.time.LocalDate assignedFrom;
    @NotNull private java.time.LocalDate assignedTo;

}
