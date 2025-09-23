package huy.example.demoMonday.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Giới hạn: 5 lần sai trong 15 phút -> khoá 15 phút.
 * Key = usernameOrEmail|ip (giảm dò username; vẫn chặn theo IP).
 * Có thể thay backend bằng Redis nếu cần scale.
 */
@Component
public class LoginRateLimiter {

    private static final class Bucket {
        int failCount;
        Instant firstFailAt;   // mốc bắt đầu cửa sổ đếm
        Instant lockUntil;     // nếu != null và > now => đang bị khoá
    }

    private final ConcurrentHashMap<String, Bucket> store = new ConcurrentHashMap<>();

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.window-minutes:15}")
    private int windowMinutes;

    @Value("${security.login.lock-minutes:15}")
    private int lockMinutes;

    private String k(String userOrEmailLower, String ip){
        return (userOrEmailLower == null ? "unknown" : userOrEmailLower.trim().toLowerCase())
                + "|" + (ip == null ? "unknown" : ip);
    }

    public boolean isLocked(String userOrEmailLower, String ip){
        var b = store.get(k(userOrEmailLower, ip));
        if (b == null) return false;
        return b.lockUntil != null && b.lockUntil.isAfter(Instant.now());
    }

    public Instant lockedUntil(String userOrEmailLower, String ip){
        var b = store.get(k(userOrEmailLower, ip));
        return b == null ? null : b.lockUntil;
    }

    /** Ghi nhận thất bại: tăng count, reset cửa sổ nếu đã quá hạn; nếu >= max -> khoá */
    public void recordFailure(String userOrEmailLower, String ip){
        var now = Instant.now();
        var key = k(userOrEmailLower, ip);
        store.compute(key, (kk, old) -> {
            var b = Objects.requireNonNullElseGet(old, Bucket::new);
            // nếu đang khoá thì giữ nguyên lock (khoá tiếp không cần)
            if (b.lockUntil != null && b.lockUntil.isAfter(now)) return b;

            if (b.firstFailAt == null || Duration.between(b.firstFailAt, now).toMinutes() >= windowMinutes) {
                b.firstFailAt = now;
                b.failCount = 1;
            } else {
                b.failCount += 1;
            }

            if (b.failCount >= maxAttempts) {
                b.lockUntil = now.plus(Duration.ofMinutes(lockMinutes));
            }
            return b;
        });
    }

    /** Reset đếm khi đăng nhập thành công */
    public void reset(String userOrEmailLower, String ip){
        store.remove(k(userOrEmailLower, ip));
    }

    /** (tuỳ chọn) lấy số lần còn lại trong cửa sổ */
    public int remainingAttempts(String userOrEmailLower, String ip){
        var b = store.get(k(userOrEmailLower, ip));
        if (b == null) return maxAttempts;
        if (b.lockUntil != null && b.lockUntil.isAfter(Instant.now())) return 0;
        return Math.max(0, maxAttempts - b.failCount);
    }
}
