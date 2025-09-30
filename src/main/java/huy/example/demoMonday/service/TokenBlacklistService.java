package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.TokenBlacklist;
import huy.example.demoMonday.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final TokenBlacklistRepository repo;

    public void blacklist(String jti, Instant expiresAt) {
        if (jti == null || repo.existsByJti(jti)) return;
        repo.save(TokenBlacklist.builder().jti(jti)
                .expiresAt(expiresAt != null ? expiresAt : Instant.now().plusSeconds(60))
                .build());
    }

    public boolean isBlacklisted(String jti) {
        return jti != null && repo.existsByJti(jti);
    }

    @Scheduled(cron = "0 5 3 * * *") // dọn rác 03:05 hằng ngày
    public void cleanup() {
        repo.deleteByExpiresAtBefore(Instant.now());
    }
}
