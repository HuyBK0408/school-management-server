package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolYearReq {
    @NotNull
    private String code;
    @NotNull private java.time.LocalDate startDate;
    @NotNull private java.time.LocalDate endDate;
    @NotNull private java.util.UUID schoolId;

}