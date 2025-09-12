package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LessonJournalReq {
    @NotNull
    private java.util.UUID classId;
    @NotNull private java.util.UUID subjectId;
    @NotNull private java.util.UUID teacherId;
    @NotNull private java.time.LocalDate teachDate;
    @NotNull private Integer periodNo;
    @NotNull private String content;
    private java.util.UUID transferredFromTeacherId;

}
