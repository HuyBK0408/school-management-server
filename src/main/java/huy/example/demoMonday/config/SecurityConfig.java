package huy.example.demoMonday.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;
    private final HandlerExceptionResolver resolver;

    public SecurityConfig(
            JwtDecoder jwtDecoder,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        this.jwtDecoder = jwtDecoder;
        this.resolver = resolver;
    }

    @Value("${app.cors.origins:*}")
    private String corsOrigins;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login2",
                                "/auth/refresh",
                                "/auth/introspect",
                                "/auth/verify-email",
                                "/auth/resend-verify",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        ).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/actuator/**",
                                "/files/**",
                                "/auth/register/student",
                                "/auth/register/teacher",
                                "/auth/register/parent"
                        ).permitAll()

                        // Ví dụ rule module school
                        .requestMatchers(HttpMethod.POST,   "/api/v1/schools/**").hasAnyRole("SYSTEM_ADMIN","SCHOOL_ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/schools/**").hasAnyRole("SYSTEM_ADMIN","SCHOOL_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/schools/**").hasRole("SYSTEM_ADMIN")

                        // Khu admin tổng
                        .requestMatchers("/auth/register/admin/**", "/admin/**").hasRole("SYSTEM_ADMIN")

                        .anyRequest().authenticated()
                )
                // Đưa lỗi 401/403 về GlobalExceptionHandler
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> resolver.resolveException(req, res, null, e)) // 401
                        .accessDeniedHandler((req, res, e) -> resolver.resolveException(req, res, null, e))       // 403
                )
                .oauth2ResourceServer(o -> o
                        // ĐẨY lỗi 401/403 về GlobalExceptionHandler
                        .authenticationEntryPoint((req, res, ex) -> resolver.resolveException(req, res, null, ex))
                        .accessDeniedHandler((req, res, ex) -> resolver.resolveException(req, res, null, ex))
                        .jwt(j -> j
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthConverter())
                        )
                );


        return http.build();
    }

    /** Map scope/roles/authorities -> ROLE_*  */
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        var c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return c;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<String> raw = new LinkedHashSet<>();

        List<String> scopeList = jwt.getClaimAsStringList("scope");
        if (scopeList != null) raw.addAll(scopeList);

        String scopeStr = jwt.getClaimAsString("scope");
        if (scopeStr != null) raw.addAll(Arrays.asList(scopeStr.split("\\s+")));

        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) raw.addAll(roles);

        List<String> authoritiesClaim = jwt.getClaimAsStringList("authorities");
        if (authoritiesClaim != null) raw.addAll(authoritiesClaim);

        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        if ("*".equals(corsOrigins)) cfg.setAllowedOriginPatterns(List.of("*"));
        else cfg.setAllowedOriginPatterns(Arrays.asList(corsOrigins.split(",")));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
