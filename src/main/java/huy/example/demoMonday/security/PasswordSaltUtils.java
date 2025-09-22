package huy.example.demoMonday.security;

import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordSaltUtils {

    private static final SecureRandom RNG = new SecureRandom();

    private PasswordSaltUtils() {}

    /**
     * Tạo salt ngẫu nhiên (Base64) với số byte chỉ định (khuyên dùng 32).
     */
    public static String newUserSaltBase64(int bytes) {
        byte[] buf = new byte[bytes];
        RNG.nextBytes(buf);
        return Base64.getEncoder().encodeToString(buf);
    }
}
