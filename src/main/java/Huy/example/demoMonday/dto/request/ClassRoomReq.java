package huy.example.demoMonday.dto.request;

import jakarta.persistence.GeneratedValue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClassRoomReq {
    @NotNull
    private String name;
    @NotNull private java.util.UUID gradeLevelId;
    @NotNull private java.util.UUID schoolYearId;
    @NotNull private java.util.UUID schoolId;
    private java.util.UUID homeroomTeacherId;

}
