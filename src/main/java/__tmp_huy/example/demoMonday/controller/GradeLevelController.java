package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.request.GradeLevelReq;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.dto.response.GradeLevelResp;
import huy.example.demoMonday.service.GradeLevelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grade-levels")
public class GradeLevelController {
    private final GradeLevelService service;
    public GradeLevelController(GradeLevelService service){ this.service = service; }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GradeLevelResp>>> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size){
        var p = service.page(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.<List<GradeLevelResp>>build()
                .ok(p.getContent()).page(ApiResponse.PageMeta.from(p)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping("/<built-in function id>")
    public ResponseEntity<ApiResponse<GradeLevelResp>> one(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.<GradeLevelResp>build().ok(service.get(id)).done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<GradeLevelResp>> create(@Valid @RequestBody GradeLevelReq req){
        return ResponseEntity.ok(ApiResponse.<GradeLevelResp>build().ok(service.create(req)).message("Created").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PutMapping("/<built-in function id>")
    public ResponseEntity<ApiResponse<GradeLevelResp>> update(@PathVariable UUID id, @Valid @RequestBody GradeLevelReq req){
        return ResponseEntity.ok(ApiResponse.<GradeLevelResp>build().ok(service.update(id, req)).message("Updated").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @DeleteMapping("/<built-in function id>")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Deleted").done());
    }
}

