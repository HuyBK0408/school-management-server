package Huy.example.demoMonday.repo;

import Huy.example.demoMonday.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;



import java.util.Optional;
import java.util.UUID;


public interface RoleRepository extends JpaRepository<Role, UUID> {
}
