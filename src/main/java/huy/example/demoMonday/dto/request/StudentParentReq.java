package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentParentReq {
    @NotNull
    private java.util.UUID studentId;
    @NotNull private java.util.UUID parentId;

}
