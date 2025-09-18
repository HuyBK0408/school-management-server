package huy.example.demoMonday.repository;


import huy.example.demoMonday.entity.UserAccount;
import huy.example.demoMonday.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    // dùng mapping theo object
    boolean existsByUser_IdAndRole_Id(UUID userId, UUID roleId);

    // để biết đã có ai có role này chưa (cho case single-admin)
    boolean existsByRole_Code(String code);

    // nếu bạn vẫn muốn lấy 1 user của 1 role:
    @Query("select ur.user from UserRole ur where ur.role.id = :roleId")
    Optional<UserAccount> findFirstUserByRoleId(@Param("roleId") UUID roleId);
    @Query("select r.code from UserRole ur join ur.role r where ur.user.id = :userId")
    List<String> findRoleCodesByUserId(@Param("userId") UUID userId);
}
