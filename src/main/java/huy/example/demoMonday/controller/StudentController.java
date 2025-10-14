package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.request.*;

import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.dto.response.StudentResp;
import huy.example.demoMonday.service.StudentDeletionService;
import huy.example.demoMonday.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;
    private final StudentDeletionService deletionService;

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResp>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var p = service.page(PageRequest.of(page, size));
        return ResponseEntity.ok(
                ApiResponse.<List<StudentResp>>build()
                        .ok(p.getContent())
                        .page(ApiResponse.PageMeta.from(p))
                        .done()
        );
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResp>> one(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<StudentResp>build()
                        .ok(service.get(id))
                        .done()
        );
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<StudentResp>> create(@Valid @RequestBody StudentReq req) {
        return ResponseEntity.ok(
                ApiResponse.<StudentResp>build()
                        .ok(service.create(req))
                        .message("Created")
                        .done()
        );
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResp>> update(@PathVariable UUID id,
                                                           @Valid @RequestBody StudentReq req) {
        return ResponseEntity.ok(
                ApiResponse.<StudentResp>build()
                        .ok(service.update(id, req))
                        .message("Updated")
                        .done()
        );
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>build()
                        .ok()
                        .message("Deleted")
                        .done()
        );
    }



    /* ============================
       CÁCH A: PUT subresource
       ============================ */

    /** 1) Chuyển lớp — PUT /{id}/classroom (idempotent, sát nghĩa) */
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @PutMapping("/{id}/classroom")
    public ResponseEntity<ApiResponse<StudentResp>> changeClassroom(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeClassroomReq req
    ) {
        deletionService.transfer(id, new TransferStudentReq(req.targetClassId(), req.note()));
        var updated = service.get(id);
        return ResponseEntity.ok(
                ApiResponse.<StudentResp>build()
                        .ok(updated)
                        .message("Classroom updated")
                        .done()
        );
    }

    /** 2) Đổi trạng thái — PUT /{id}/status (EXPELLED | GRADUATED) */
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<StudentResp>> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusReq req
    ) {
        switch (req.value()) {
            case EXPELLED -> deletionService.expel(id, req.toExpelReq());
            case GRADUATED -> deletionService.graduate(id, req.toGraduateReq());
            default -> throw new IllegalArgumentException("Unsupported status value: " + req.value());
        }
        var updated = service.get(id);
        return ResponseEntity.ok(
                ApiResponse.<StudentResp>build()
                        .ok(updated)
                        .message("Status updated")
                        .done()
        );
    }

    /* ❌ NHỚ XÓA: method @PatchMapping("/{id}/lifecycle") nếu bạn đang giữ để tránh trùng & rối Swagger */
}
