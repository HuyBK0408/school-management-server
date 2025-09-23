package huy.example.demoMonday.service;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;


@Service
public class JwtService {
    @Value("${spring.security.jwt.secret:}")
    private String secret;


    @Value("${spring.security.jwt.access-minutes:1}") // set 1 phút để test; đổi trong yml khi cần
    private int accessMinutes;


    private Key key;


    @jakarta.annotation.PostConstruct
    void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT secret quá ngắn (<32 bytes). Hãy đặt biến môi trường JWT_SECRET.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generate(String username, List<String> roles){
        var now = new Date();
        var exp = new Date(now.getTime() + accessMinutes * 60_000L);
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public Jws<Claims> parse(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}