package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    Optional<UserAccount> findByUsernameOrEmail(String username, String email);
    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);

}
