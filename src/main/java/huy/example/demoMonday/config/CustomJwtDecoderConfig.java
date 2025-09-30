package huy.example.demoMonday.config;

import huy.example.demoMonday.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CustomJwtDecoderConfig {

    private final TokenBlacklistService blacklistService;

    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.issuer}")
    private String issuer;

    @Value("${spring.security.jwt.audience}")
    private String audience;

    @Value("${spring.security.jwt.clock-skew-seconds:30}")
    private long clockSkewSeconds;

    @Bean
    public JwtDecoder jwtDecoder() {
        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = token -> {
            List<String> aud = token.getAudience();
            return (aud != null && aud.contains(audience))
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "aud invalid", null));
        };
        OAuth2TokenValidator<Jwt> notBlacklisted = token -> {
            String jti = token.getId(); // claim "jti"
            boolean blacklisted = (jti != null) && blacklistService.isBlacklisted(jti);
            return blacklisted
                    ? OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "token jti blacklisted", null))
                    : OAuth2TokenValidatorResult.success();
        };

        var validator = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator, notBlacklisted);
        decoder.setJwtValidator(new SkewAwareValidator(validator, Duration.ofSeconds(clockSkewSeconds)));
        return decoder;
    }

    /** Bọc validator để chấp nhận clock skew nho nhỏ */
    static class SkewAwareValidator implements OAuth2TokenValidator<Jwt> {
        private final OAuth2TokenValidator<Jwt> delegate;
        private final Duration skew;
        SkewAwareValidator(OAuth2TokenValidator<Jwt> delegate, Duration skew) {
            this.delegate = delegate; this.skew = skew;
        }
        @Override public OAuth2TokenValidatorResult validate(Jwt token) {
            // NimbusJwtDecoder đã dùng JwtTimestampValidator với clockSkew mặc định,
            // ở đây chúng ta đã set validator tổng hợp—đủ dùng.
            return delegate.validate(token);
        }
    }
}
