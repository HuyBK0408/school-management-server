package huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusReq(
        @NotNull Value value,   // EXPELLED | GRADUATED
        String reason,          // cho EXPELLED
        Boolean banLogin,       // cho EXPELLED (default true)
        Boolean issueDiploma,   // cho GRADUATED (default true)
        String note             // cho GRADUATED
) {
    public enum Value { EXPELLED, GRADUATED }

    public ExpelStudentReq toExpelReq() {
        return new ExpelStudentReq(reason, banLogin);
    }
    public GraduateStudentReq toGraduateReq() {
        return new GraduateStudentReq(issueDiploma, note);
    }
}
