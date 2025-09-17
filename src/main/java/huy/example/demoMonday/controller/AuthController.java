package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.request.LoginRequest;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.security.JwtService;
import huy.example.demoMonday.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor

public class AuthController {
    private final JwtService jwt;
    private final AuthService authService;
    ///public AuthController(JwtService jwt){ this.jwt = jwt; }

    @PostMapping("/login-demo")
    public ResponseEntity<ApiResponse<String>> loginDemo(@RequestParam String username, @RequestParam List<String> roles) {
        String token = jwt.generate(username, roles);
        return ResponseEntity.ok(ApiResponse.<String>build().ok(token).message("DEMO token").done());
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(ApiResponse.<String>build().ok(token).message("OK").done());
    }
}

