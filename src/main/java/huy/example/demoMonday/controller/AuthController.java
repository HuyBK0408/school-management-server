package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.auth.*;
import huy.example.demoMonday.dto.request.LoginRequest;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.security.JwtService;
import huy.example.demoMonday.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwt;
    private final AuthService authService;

    // DEMO (giữ nguyên)
    @PostMapping("/login-demo")
    public ResponseEntity<ApiResponse<String>> loginDemo(@RequestParam String username, @RequestParam List<String> roles) {
        String token = jwt.generate(username, roles);
        return ResponseEntity.ok(ApiResponse.<String>build().ok(token).message("DEMO token").done());
    }

    // Login cũ (trả về access token ngắn gọn)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(ApiResponse.<String>build().ok(token).message("OK").done());
    }

    // ===== New: Full auth flow =====

    // Đăng ký
    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<Void>> registerStudent(@Valid @RequestBody StudentRegisterReq req){
        authService.registerStudent(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok(null).message("Đã gửi mã xác thực email").done());
    }
    @PostMapping("/register/teacher")
    public ResponseEntity<ApiResponse<Void>> registerTeacher(@Valid @RequestBody TeacherRegisterReq req){
        authService.registerTeacher(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok(null).message("Đã gửi mã xác thực email").done());
    }
    @PostMapping("/register/parent")
    public ResponseEntity<ApiResponse<Void>> registerParent(@Valid @RequestBody ParentRegisterReq req){
        authService.registerParent(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok(null).message("Đã gửi mã xác thực email").done());
    }

    // Xác thực email
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verify(@Valid @RequestBody VerifyEmailReq req){
        authService.verifyEmail(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok(null).message("Xác thực email thành công").done());
    }

    // Login mới (username/email + password) -> access + refresh
    @PostMapping("/login2")
    public ResponseEntity<ApiResponse<Map<String,String>>> login2(@Valid @RequestBody LoginReq req){
        var tokens = authService.login2(req);
        return ResponseEntity.ok(ApiResponse.<Map<String,String>>build().ok(tokens).message("OK").done());
    }

    // Refresh
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String,String>>> refresh(@RequestParam String refreshToken){
        var out = authService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.<Map<String,String>>build().ok(out).message("OK").done());
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(name="Authorization", required=false) String authz,
                                                    @RequestParam(required=false) String refreshToken){
        String access = (authz!=null && authz.startsWith("Bearer ")) ? authz.substring(7) : null;
        authService.logout(access, refreshToken);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok(null).message("Đã đăng xuất").done());
    }

    // Quên/đặt lại mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgot(@Valid @RequestBody ForgotPasswordReq req){
        authService.forgotPassword(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok(null).message("Đã gửi mã qua email").done());
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> reset(@Valid @RequestBody ResetPasswordReq req){
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok(null).message("Đặt lại mật khẩu thành công").done());
    }
}
