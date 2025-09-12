package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectReq {
    @NotNull
    private String name;
    @NotNull private String code;
    @NotNull private java.util.UUID schoolId;

}
