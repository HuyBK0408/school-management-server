package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermReq {
    @NotNull
    private String name;
    @NotNull private Integer orderNo;
    @NotNull private java.util.UUID schoolYearId;

}
