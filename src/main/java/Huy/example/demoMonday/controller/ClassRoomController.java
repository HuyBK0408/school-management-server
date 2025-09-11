package Huy.example.demoMonday.controller;

import Huy.example.demoMonday.dto.request.ClassRoomReq;
import Huy.example.demoMonday.dto.response.ApiResponse;
import Huy.example.demoMonday.dto.response.ClassRoomResp;
import Huy.example.demoMonday.service.ClassRoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classrooms")
public class ClassRoomController {
    private final ClassRoomService service;
    public ClassRoomController(ClassRoomService service){ this.service = service; }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassRoomResp>>> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size){
        var p = service.page(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.<List<ClassRoomResp>>build()
                .ok(p.getContent()).page(ApiResponse.PageMeta.from(p)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping("/<built-in function id>")
    public ResponseEntity<ApiResponse<ClassRoomResp>> one(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.<ClassRoomResp>build().ok(service.get(id)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<ClassRoomResp>> create(@Valid @RequestBody ClassRoomReq req){
        return ResponseEntity.ok(ApiResponse.<ClassRoomResp>build().ok(service.create(req)).message("Created").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PutMapping("/<built-in function id>")
    public ResponseEntity<ApiResponse<ClassRoomResp>> update(@PathVariable UUID id, @Valid @RequestBody ClassRoomReq req){
        return ResponseEntity.ok(ApiResponse.<ClassRoomResp>build().ok(service.update(id, req)).message("Updated").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @DeleteMapping("/<built-in function id>")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Deleted").done());
    }
}

