package Huy.example.demoMonday.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ReportCardResp(
        UUID id, UUID studentId, UUID schoolYearId,
        BigDecimal overallAvg, Huy.example.demoMonday.enums.Conduct overallConduct,
        String homeroomComment, String parentComment
) {}
