package Huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingAssignmentResp {
    private java.util.UUID id;
    private java.util.UUID teacherId;
    private java.util.UUID classId;
    private java.util.UUID subjectId;
    private java.util.UUID schoolYearId;
    private java.time.LocalDate assignedFrom;
    private java.time.LocalDate assignedTo;
}
