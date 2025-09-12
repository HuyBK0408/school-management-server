package huy.example.demoMonday.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeAggregateResp(
        UUID id, UUID studentId, UUID subjectId, UUID termId,
        BigDecimal avgScore, huy.example.demoMonday.enums.Conduct conduct, String teacherComment, String parentComment
) {}
