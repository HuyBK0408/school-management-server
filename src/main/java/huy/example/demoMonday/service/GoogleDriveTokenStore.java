package huy.example.demoMonday.service;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class GoogleDriveTokenStore {
    private final AtomicReference<String> refreshToken = new AtomicReference<>(null);
    public void saveRefreshToken(String token) { refreshToken.set(token); }
    public String getRefreshToken() { return refreshToken.get(); }
    public boolean hasToken() { return refreshToken.get() != null && !refreshToken.get().isBlank(); }
}
