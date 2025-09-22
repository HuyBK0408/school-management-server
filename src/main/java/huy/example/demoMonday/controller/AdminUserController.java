package huy.example.demoMonday.controller;


import huy.example.demoMonday.dto.auth.AdminCreateUserReq;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.entity.UserAccount;
import huy.example.demoMonday.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserCreatedDto>> create(@Valid @RequestBody AdminCreateUserReq req) {
        UserAccount u = authService.adminCreateUser(req); // đã gửi email verify
        var body = new UserCreatedDto(u.getId().toString(), u.getUsername(), u.getEmail());
        return ResponseEntity.ok(
                ApiResponse.<UserCreatedDto>build().ok(body)
                        .message("Đã tạo user. Vui lòng kiểm tra email để xác thực.")
                        .done()
        );
    }

    public record UserCreatedDto(String id, String username, String email) {}
}
