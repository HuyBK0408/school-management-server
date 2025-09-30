package huy.example.demoMonday.exception;

import huy.example.demoMonday.dto.response.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =============== 4xx nhóm VALIDATION / REQUEST ===============

    // 400 - @Valid trên @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        log.warn("[{} {}] validation body: {}", req.getMethod(), req.getRequestURI(), errors);
        return ApiResponse.<Map<String, String>>build()
                .fail("VALIDATION_ERROR", "Dữ liệu không hợp lệ")
                .data(errors)
                .done();
    }

    // 400 - @Validated trên @RequestParam/@PathVariable, hoặc bean validation nổ trong service
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("[{} {}] validation param/path: {}", req.getMethod(), req.getRequestURI(), msg);
        return ApiResponse.<String>build().fail("VALIDATION_ERROR", msg).done();
    }

    // 400 - binding error (query/path/form – không qua @RequestBody)
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleBindException(BindException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        log.warn("[{} {}] bind error: {}", req.getMethod(), req.getRequestURI(), errors);
        return ApiResponse.<Map<String, String>>build().fail("VALIDATION_ERROR", "Dữ liệu không hợp lệ").data(errors).done();
    }

    // 400 - thiếu request param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Thiếu tham số bắt buộc: " + ex.getParameterName();
        log.warn("[{} {}] {}", req.getMethod(), req.getRequestURI(), msg);
        return ApiResponse.<String>build().fail("BAD_REQUEST", msg).done();
    }

    // 400 - sai kiểu tham số (?page=abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Sai kiểu tham số: " + ex.getName();
        log.warn("[{} {}] {} ({})", req.getMethod(), req.getRequestURI(), msg, ex.getMessage());
        return ApiResponse.<String>build().fail("BAD_REQUEST", msg).done();
    }

    // 400 - JSON body không parse được
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("[{} {}] unreadable body: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<String>build().fail("BAD_REQUEST", "Payload không hợp lệ").done();
    }

    // 400 - lỗi validation trong transaction
    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleTxViolation(TransactionSystemException ex, HttpServletRequest req) {
        Throwable root = ex.getRootCause();
        if (root instanceof ConstraintViolationException cve) {
            String msg = cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            log.warn("[{} {}] tx validation: {}", req.getMethod(), req.getRequestURI(), msg);
            return ApiResponse.<String>build().fail("VALIDATION_ERROR", msg).done();
        }
        log.warn("[{} {}] tx error: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<String>build().fail("BAD_REQUEST", ex.getMessage()).done();
    }

    // 404
    @ExceptionHandler({EntityNotFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(Exception ex, HttpServletRequest req) {
        log.warn("[{} {}] not found: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<Void>build().fail("NOT_FOUND", "Không tìm thấy tài nguyên").done();
    }

    // 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("[{} {}] method not allowed: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<String>build().fail("METHOD_NOT_ALLOWED", "Phương thức không được hỗ trợ").done();
    }

    // 401 - Security auth
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        log.warn("[{} {}] auth error: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<String>build().fail("UNAUTHORIZED", "Thông tin đăng nhập không hợp lệ").done();
    }

    @ExceptionHandler({ JwtValidationException.class, BadJwtException.class, JwtException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<String> handleSpringJwt(JwtException ex, HttpServletRequest req) {
        String code = (ex instanceof JwtValidationException) ? "JWT_EXPIRED_OR_INVALID" : "JWT_INVALID";
        log.warn("[{} {}] {}: {}", req.getMethod(), req.getRequestURI(), code, ex.getMessage());
        return ApiResponse.<String>build().fail(code, "Token không hợp lệ").done();
    }

    // 403 - thiếu quyền
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("[{} {}] access denied", req.getMethod(), req.getRequestURI());
        return ApiResponse.<Void>build().fail("FORBIDDEN", "Không có quyền truy cập").done();
    }

    // 409 - ràng buộc CSDL
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<String> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("[{} {}] db integrity: {}", req.getMethod(), req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return ApiResponse.<String>build().fail("DATA_INTEGRITY_VIOLATION", "Dữ liệu xung đột hoặc ràng buộc CSDL").done();
    }

    // 413 - upload quá lớn
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiResponse<String> handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        log.warn("[{} {}] upload too large: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<String>build().fail("PAYLOAD_TOO_LARGE", "Dung lượng upload vượt giới hạn").done();
    }

    // 400 - lỗi multipart khác
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleMultipart(MultipartException ex, HttpServletRequest req) {
        log.warn("[{} {}] multipart error: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<String>build().fail("BAD_REQUEST", "Tải lên tệp không hợp lệ").done();
    }

    // =============== ResponseStatusException (trả đúng HTTP status) ===============
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<String>> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (status.is5xxServerError()) {
            log.error("[{} {}] {}", req.getMethod(), req.getRequestURI(), ex.getReason(), ex);
        } else {
            log.warn("[{} {}] {}", req.getMethod(), req.getRequestURI(), ex.getReason());
        }

        String code = mapStatusToCode(status);
        var body = ApiResponse.<String>build()
                .fail(code, ex.getReason() != null ? ex.getReason() : status.getReasonPhrase())
                .done();

        return ResponseEntity.status(status).body(body);
    }

    private String mapStatusToCode(HttpStatus st) {
        return switch (st) {
            case BAD_REQUEST -> "BAD_REQUEST";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            case METHOD_NOT_ALLOWED -> "METHOD_NOT_ALLOWED";
            case CONFLICT -> "DATA_INTEGRITY_VIOLATION";
            case PAYLOAD_TOO_LARGE -> "PAYLOAD_TOO_LARGE";
            case TOO_MANY_REQUESTS -> "TOO_MANY_REQUESTS";
            default -> st.is4xxClientError() ? "BAD_REQUEST" : "INTERNAL_ERROR";
        };
    }

    // =============== 5xx FALLBACK ===============
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("[{} {}] unexpected error", req.getMethod(), req.getRequestURI(), ex);
        return ApiResponse.<String>build()
                .fail("INTERNAL_ERROR", "Có lỗi xảy ra, vui lòng thử lại hoặc liên hệ hỗ trợ")
                .done();
    }
}
