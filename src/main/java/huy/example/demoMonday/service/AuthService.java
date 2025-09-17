package huy.example.demoMonday.service;

import huy.example.demoMonday.repo.UserAccountRepository;
import huy.example.demoMonday.repo.UserRoleRepository;
import huy.example.demoMonday.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    public String login(String username, String rawPassword) {
        var user = userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isEnabled()) {
            throw new RuntimeException("User disabled");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        var roles = userRoleRepo.findRoleCodesByUserId(user.getId());
        return jwt.generate(user.getUsername(), roles);
    }
}

