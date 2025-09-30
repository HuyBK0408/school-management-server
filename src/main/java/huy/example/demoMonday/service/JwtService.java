package huy.example.demoMonday.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Service
public class JwtService {

    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.issuer}")
    private String issuer;

    @Value("${spring.security.jwt.audience}")
    private String audience;

    @Value("${spring.security.jwt.access-minutes:15}")
    private long accessMinutes;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, List<String> roles) {
        var now = new Date();
        var exp = new Date(now.getTime() + accessMinutes * 60_000L);
        String jti = java.util.UUID.randomUUID().toString();

        List<String> scope = new ArrayList<>();
        for (String r : roles) scope.add("ROLE_" + r);

        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", scope);
        claims.put("roles", roles);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(exp)
                .setId(jti)
                .addClaims(claims)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
}
