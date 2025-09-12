package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeLevelReq {
    @NotNull private String name;
    @NotNull
    private java.util.UUID schoolId;

}
