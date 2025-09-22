package huy.example.demoMonday.security;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProtectedAdminGuard {

    private final Environment env;

    private Set<String> emails;     // lower-case
    private Set<String> usernames;  // lower-case

    @PostConstruct
    public void load() {
        // Ví dụ cấu hình:
        // PROTECTED_ADMIN_EMAILS=admin@example.com,ops@example.com
        // PROTECTED_ADMIN_USERNAMES=admin,ops
        emails = parseCsv(env.getProperty("PROTECTED_ADMIN_EMAILS", ""));
        usernames = parseCsv(env.getProperty("PROTECTED_ADMIN_USERNAMES", ""));
    }

    private Set<String> parseCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public boolean isProtected(String username, String email) {
        if (username != null && usernames.contains(username.toLowerCase())) return true;
        if (email != null && emails.contains(email.toLowerCase())) return true;
        return false;
    }
}
