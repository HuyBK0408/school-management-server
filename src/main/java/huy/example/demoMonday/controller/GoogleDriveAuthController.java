package huy.example.demoMonday.controller;

import huy.example.demoMonday.service.GoogleDriveOAuthService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/google/drive/oauth2")
public class GoogleDriveAuthController {

    private final GoogleDriveOAuthService oauth;

    @GetMapping("/init")
    @PermitAll
    public ResponseEntity<?> init() throws Exception {
        return ResponseEntity.ok().body(new java.util.LinkedHashMap<>() {{
            put("authUrl", oauth.buildAuthorizationUrl());
        }});
    }

    @GetMapping("/callback")
    @PermitAll
    public ResponseEntity<?> callback(@RequestParam String code) throws Exception {
        oauth.handleCallback(code);
        return ResponseEntity.ok().body(new java.util.LinkedHashMap<>() {{ put("status","OK"); }});
    }
}
