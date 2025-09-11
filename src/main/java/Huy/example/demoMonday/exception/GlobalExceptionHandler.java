package Huy.example.demoMonday.exception;

import Huy.example.demoMonday.dto.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> notFound(EntityNotFoundException ex, HttpServletRequest req) {
        log.warn("[{} {}] {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<Void>build().fail("NOT_FOUND", ex.getMessage()).done();
    }

    // 400 — @Valid trên @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a, LinkedHashMap::new));
        log.warn("[{} {}] validation: {}", req.getMethod(), req.getRequestURI(), errors);
        return ApiResponse.<Map<String, String>>build()
                .fail("VALIDATION_ERROR", "Invalid request")
                .data(errors)
                .done();
    }

    // 400 — @Validated trên @PathVariable/@RequestParam hoặc lỗi convert kiểu
    @ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> constraint(Exception ex, HttpServletRequest req) {
        String msg = ex instanceof ConstraintViolationException cve
                ? cve.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("; "))
                : ex.getMessage();
        log.warn("[{} {}] bad request: {}", req.getMethod(), req.getRequestURI(), msg);
        return ApiResponse.<String>build().fail("BAD_REQUEST", msg).done();
    }

    // 400 — thiếu query param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> missingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.warn("[{} {}] missing param: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<Void>build().fail("BAD_REQUEST", ex.getMessage()).done();
    }

    // 400 — JSON sai cú pháp
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> badJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        log.warn("[{} {}] bad json: {}", req.getMethod(), req.getRequestURI(), msg);
        return ApiResponse.<Void>build().fail("BAD_JSON", msg).done();
    }

    // 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> methodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("[{} {}] method not allowed: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ApiResponse.<Void>build().fail("METHOD_NOT_ALLOWED", ex.getMessage()).done();
    }

    // 409/400 — vi phạm ràng buộc DB (FK/UNIQUE)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "Data constraint violated";
        log.warn("[{} {}] integrity: {}", req.getMethod(), req.getRequestURI(), msg);
        return ApiResponse.<Void>build().fail("DATA_INTEGRITY", msg).done();
    }

    // 400 — JPA validation ném qua TransactionSystemException
    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> txViolation(TransactionSystemException ex, HttpServletRequest req) {
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

    // 403 — thiếu quyền
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> accessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("[{} {}] access denied", req.getMethod(), req.getRequestURI());
        return ApiResponse.<Void>build().fail("FORBIDDEN", "Access is denied").done();
    }

    // 500 — còn lại
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> unknown(Exception ex, HttpServletRequest req) {
        log.error("[{} {}] unexpected", req.getMethod(), req.getRequestURI(), ex);
        return ApiResponse.<Void>build().fail("INTERNAL_ERROR", ex.getMessage()).done();
    }
}