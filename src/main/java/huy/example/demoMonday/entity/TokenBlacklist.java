package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(name = "idx_token_blacklist_jti", columnList = "jti", unique = true),
        @Index(name = "idx_token_blacklist_expires", columnList = "expires_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TokenBlacklist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 64, unique = true)
    private String jti;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
