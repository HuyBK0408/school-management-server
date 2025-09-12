package huy.example.demoMonday.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;


@Service
public class JwtService {
    private final String SECRET = System.getenv().getOrDefault("JWT_SECRET","CHANGE_THIS_SUPER_SECRET_256_BITS_KEY_1234567890");
    private Key key(){ return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)); }
    public String generate(String username, List<String> roles){
        var now = new Date(); var exp = new Date(now.getTime() + 1000L*60*60*8);
        return Jwts.builder().setSubject(username).claim("roles", roles).setIssuedAt(now).setExpiration(exp)
                .signWith(key(), SignatureAlgorithm.HS256).compact();
    }
    public Jws<Claims> parse(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
    }
}
