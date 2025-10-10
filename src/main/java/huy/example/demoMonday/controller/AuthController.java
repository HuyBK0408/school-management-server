package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.request.*;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.service.AuthService;
import huy.example.demoMonday.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "auth-controller")
public class AuthController {

    private final JwtService jwt;          // vẫn giữ nếu service cần, không dùng thì có thể xóa
    private final AuthService authService;

    /* ========= RESTful, tối thiểu cần thiết ========= */

    /** Đăng nhập: trả access + refresh token (tái dùng logic cũ login2 để không đổi service) */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginReq req) {
        var tokens = authService.login2(req);
        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>build().ok(tokens).message("Logged in").done()
        );
    }

    /** Refresh token: cấp cặp token mới */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(@Valid @RequestBody RefreshTokenReq req) {
        var out = authService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>build().ok(out).message("Refreshed").done()
        );
    }

    /** Logout: revoke refresh / blacklist access (đọc access từ Authorization nếu có) */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(name = "Authorization", required = false) String authz,
            @Valid @RequestBody(required = false) RefreshTokenReq req
    ) {
        String access = (authz != null && authz.startsWith("Bearer ")) ? authz.substring(7) : null;
        String refresh = (req != null ? req.getRefreshToken() : null);
        authService.logout(access, refresh);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Logged out").done());
    }

    /** Quên mật khẩu: gửi email chứa mã/link đặt lại */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgot(@Valid @RequestBody ForgotPasswordReq req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Reset link sent").done());
    }

    /** Đặt lại mật khẩu bằng mã/token hợp lệ */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> reset(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Password reset").done());
    }

    /** Xác minh email bằng mã đã gửi */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verify(@Valid @RequestBody VerifyEmailReq req) {
        authService.verifyEmail(req);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Email verified").done());
    }

    /** Gửi lại mã xác minh (path gộp theo nhóm verify-email cho chuẩn) */
    @PostMapping("/verify-email/resend")
    public ResponseEntity<ApiResponse<Void>> resend(@Valid @RequestBody ResendVerifyReq req) {
        authService.resendVerifyEmail(req.email());
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Verification sent").done());
    }

    /* ========= (Tuỳ chọn) Đăng ký public 1 endpoint duy nhất =========
     * Nếu cần sau này, hãy tạo DTO PublicRegisterReq và service.registerPublic(req), rồi mở route dưới:
     *
     * @PostMapping("/register")
     * public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody PublicRegisterReq req) {
     *     authService.registerPublic(req);
     *     return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Registered. Please verify email.").done());
     * }
     */
}
