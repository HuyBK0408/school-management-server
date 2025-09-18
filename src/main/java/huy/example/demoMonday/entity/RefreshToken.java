package huy.example.demoMonday.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity @Getter @Setter
public class RefreshToken extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional=false) private UserAccount user;
    @Column(nullable=false, unique=true, length=200) private String tokenHash;
    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false) private boolean revoked = false;
}
