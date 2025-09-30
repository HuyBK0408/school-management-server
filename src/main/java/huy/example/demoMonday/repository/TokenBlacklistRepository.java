package huy.example.demoMonday.repository;

import huy.example.demoMonday.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByJti(String jti);
    long deleteByExpiresAtBefore(java.time.Instant instant);
}
