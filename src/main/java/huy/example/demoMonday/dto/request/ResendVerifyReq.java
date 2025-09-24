// dto/request/ResendVerifyReq.java
package huy.example.demoMonday.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

public record ResendVerifyReq(
        @Schema(example = "user@example.com") String email
) {
}
