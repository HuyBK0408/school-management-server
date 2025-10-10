package huy.example.demoMonday.dto.request;

public record GraduateStudentReq(
        Boolean issueDiploma, // null => mặc định true tại service
        String note
) {}
