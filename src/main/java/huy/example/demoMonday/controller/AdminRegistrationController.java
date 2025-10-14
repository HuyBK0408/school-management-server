package huy.example.demoMonday.controller;



import huy.example.demoMonday.dto.request.PublicRegisterReq;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.entity.UserAccount;
import huy.example.demoMonday.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.net.URI;


@RestController
@RequestMapping("/api/v1/admin/register")
@RequiredArgsConstructor
public class AdminRegistrationController {
    private final AuthService authService;


    /**
     * Admin can register STUDENT/TEACHER/PARENT in one place.
     * Uses the same command shape as public register.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    public ResponseEntity<ApiResponse<UserCreatedDto>> registerByAdmin(@Valid @RequestBody PublicRegisterReq cmd) {
        UserAccount created = authService.registerPublic(cmd); // same service, same validation
        var location = URI.create("/api/v1/users/" + created.getId());
        var body = new UserCreatedDto(created.getId().toString(), created.getUsername(), created.getEmail(), cmd.role().name());
        return ResponseEntity.created(location)
                .body(ApiResponse.<UserCreatedDto>build().ok(body).message("Created by admin").done());
    }


    public record UserCreatedDto(String id, String username, String email, String role) {}
}