package huy.example.demoMonday.dto.request;

public record ExpelStudentReq(
        String reason,
        Boolean banLogin // null => mặc định true tại service
) {}
