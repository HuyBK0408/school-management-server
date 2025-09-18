package huy.example.demoMonday.repository;

import huy.example.demoMonday.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {
    boolean existsByTokenHash(String tokenHash);
}
