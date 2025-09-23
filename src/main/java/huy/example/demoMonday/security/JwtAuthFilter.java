package huy.example.demoMonday.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import huy.example.demoMonday.repository.TokenBlacklistRepository;
import huy.example.demoMonday.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.util.*;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final TokenBlacklistRepository blacklistRepo;


    public JwtAuthFilter(JwtService jwt, TokenBlacklistRepository blacklistRepo) {
        this.jwt = jwt; this.blacklistRepo = blacklistRepo;
    }


    private static String sha256(String s){
        try{
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for(byte b: d) sb.append(String.format("%02x", b));
            return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws java.io.IOException, jakarta.servlet.ServletException {
        String authz = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authz) && authz.startsWith("Bearer ")) {
            String token = authz.substring(7);
            try {
// ✋ Chặn token đã vào blacklist (đã logout)
                if (blacklistRepo.existsByTokenHash(sha256(token))) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    new ObjectMapper().writeValue(res.getWriter(), Map.of("error","Token has been revoked"));
                    return;
                }


                var jws = jwt.parse(token);
                var username = jws.getBody().getSubject();
                var roles = (List<?>) jws.getBody().get("roles");
                var authorities = new ArrayList<SimpleGrantedAuthority>();
                if (roles != null) roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_"+r)));
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                new ObjectMapper().writeValue(res.getWriter(), Map.of("error","Invalid or expired token"));
                return;
            }
        }
        chain.doFilter(req, res);
    }
}