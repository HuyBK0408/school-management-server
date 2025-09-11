package Huy.example.demoMonday.controller;

import Huy.example.demoMonday.dto.response.ApiResponse;
import Huy.example.demoMonday.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwt;
    public AuthController(JwtService jwt){ this.jwt = jwt; }

    @PostMapping("/login-demo")
    public ResponseEntity<ApiResponse<String>> loginDemo(@RequestParam String username, @RequestParam List<String> roles) {
        String token = jwt.generate(username, roles);
        return ResponseEntity.ok(ApiResponse.<String>build().ok(token).message("DEMO token").done());
    }
}

