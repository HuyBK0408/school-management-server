package huy.example.demoMonday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import huy.example.demoMonday.dto.request.AdminCreateUserReq;
import huy.example.demoMonday.dto.request.ParentReq;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.dto.response.ParentResp;
import huy.example.demoMonday.service.ParentService;
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
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService service;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ParentResp>>> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size){
        var p = service.page(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.<List<ParentResp>>build()
                .ok(p.getContent()).page(ApiResponse.PageMeta.from(p)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParentResp>> one(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.<ParentResp>build().ok(service.get(id)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<ParentResp>> create(@Valid @RequestBody ParentReq req){
        return ResponseEntity.ok(ApiResponse.<ParentResp>build().ok(service.create(req)).message("Created").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ParentResp>> update(@PathVariable UUID id, @Valid @RequestBody ParentReq req){
        return ResponseEntity.ok(ApiResponse.<ParentResp>build().ok(service.update(id, req)).message("Updated").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Deleted").done());
    }

    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping(value = "/{id}/account", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> createAccountJson(
            @PathVariable UUID id,
            @Valid @RequestBody AdminCreateUserReq req
    ) {
        service.createAccount(id, req, null);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Account created").done());
    }

    /** Admin tạo account cho phụ huynh – multipart + optional photo */
    @Operation(summary = "Admin tạo account cho phụ huynh (multipart: cmd JSON + optional photo)")
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
