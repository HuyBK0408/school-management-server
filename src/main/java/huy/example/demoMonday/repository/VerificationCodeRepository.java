package huy.example.demoMonday.repository;

import huy.example.demoMonday.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, UUID> {
    Optional<VerificationCode> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(String email, String type);

    Optional<VerificationCode> findByEmailAndType(String email, String signup);

    @Modifying
    @Query("delete from VerificationCode v where v.email = :email and v.type = :type")
    void deleteAllByEmailAndType(@Param("email") String email, @Param("type") String type);
}
