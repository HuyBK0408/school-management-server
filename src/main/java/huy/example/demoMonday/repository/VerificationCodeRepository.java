package huy.example.demoMonday.repository;

import huy.example.demoMonday.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, String type);
}
