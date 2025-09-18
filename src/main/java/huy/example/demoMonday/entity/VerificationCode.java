package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity @Getter @Setter
public class VerificationCode extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable=false) private String email;
    @Column(nullable=false) private String code;   // 6 ký tự
    @Column(nullable=false) private String type;   // SIGNUP / RESET
    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false) private boolean used = false;
}
