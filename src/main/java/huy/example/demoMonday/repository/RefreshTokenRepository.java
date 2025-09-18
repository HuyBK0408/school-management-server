package huy.example.demoMonday.repository;

import huy.example.demoMonday.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
