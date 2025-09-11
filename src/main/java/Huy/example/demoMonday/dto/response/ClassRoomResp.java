package Huy.example.demoMonday.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomResp {
    private java.util.UUID id;
    private String name;
    private java.util.UUID gradeLevelId;
    private java.util.UUID schoolYearId;
    private java.util.UUID schoolId;
    private java.util.UUID homeroomTeacherId;
}
