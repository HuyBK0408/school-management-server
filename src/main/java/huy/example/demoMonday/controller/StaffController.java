package huy.example.demoMonday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import huy.example.demoMonday.dto.request.AdminCreateUserReq;
import huy.example.demoMonday.dto.request.StaffReq;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.dto.response.StaffResp;
import huy.example.demoMonday.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService service;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffResp>>> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size){
        var p = service.page(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.<List<StaffResp>>build()
                .ok(p.getContent()).page(ApiResponse.PageMeta.from(p)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResp>> one(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.<StaffResp>build().ok(service.get(id)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<StaffResp>> create(@Valid @RequestBody StaffReq req){
        return ResponseEntity.ok(ApiResponse.<StaffResp>build().ok(service.create(req)).message("Created").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResp>> update(@PathVariable UUID id, @Valid @RequestBody StaffReq req){
        return ResponseEntity.ok(ApiResponse.<StaffResp>build().ok(service.update(id, req)).message("Updated").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Deleted").done());
    }

    /** ✅ chỉ gọi service – service sẽ tự tạo account & gửi email */
    /** Admin tạo account cho giáo viên/nhân sự – JSON body */
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping(value = "/{id}/account", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> createAccountJson(
            @PathVariable UUID id,
            @Valid @RequestBody AdminCreateUserReq req
    ) {
        service.createAccount(id, req, null);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Account created").done());
    }

    /** Admin tạo account cho giáo viên/nhân sự – multipart + optional photo */
    @Operation(summary = "Admin tạo account cho giáo viên/nhân sự (multipart: cmd JSON + optional photo)")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping(value = "/{id}/account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> createAccountMultipart(
            @PathVariable UUID id,
            @RequestPart("cmd")
            @Parameter(
                    description = "JSON theo AdminCreateUserReq",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AdminCreateUserReq.class)
                    )
            )
            String cmd,
            @RequestPart(value = "photo", required = false)
            @Parameter(
                    description = "Ảnh đại diện (jpeg/png/webp, ≤ 5MB)",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))
            )
            MultipartFile photo
    ) {
        try {
            AdminCreateUserReq req = objectMapper.readValue(cmd, AdminCreateUserReq.class);
            service.createAccount(id, req, photo);
            return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Account created").done());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "cmd phải là JSON hợp lệ theo AdminCreateUserReq");
        } catch (java.io.IOException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Không đọc được phần cmd/photo");
        }
    }
}
