package huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonJournalResp {
    private java.util.UUID id;
    private java.util.UUID classId;
    private java.util.UUID subjectId;
    private java.util.UUID teacherId;
    private java.time.LocalDate teachDate;
    private Integer periodNo;
    private String content;
    private java.util.UUID transferredFromTeacherId;
}
