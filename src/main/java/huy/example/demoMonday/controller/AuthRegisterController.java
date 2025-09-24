// huy/example/demoMonday/controller/AuthRegisterController.java
package huy.example.demoMonday.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import huy.example.demoMonday.dto.request.ParentRegisterReq;
import huy.example.demoMonday.dto.request.StudentRegisterReq;
import huy.example.demoMonday.dto.request.TeacherRegisterReq;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.entity.Parent;
import huy.example.demoMonday.entity.Staff;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.repository.ParentRepository;
import huy.example.demoMonday.repository.StaffRepository;
import huy.example.demoMonday.repository.StudentRepository;
import huy.example.demoMonday.repository.UserAccountRepository;
import huy.example.demoMonday.service.AuthService;
import huy.example.demoMonday.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthRegisterController {

    private final AuthService authService;
    private final ImageStorageService storage;
    private final ObjectMapper om;

    // repos để đổi ảnh sau đăng nhập & admin đổi ảnh
    private final UserAccountRepository userRepo;
    private final StudentRepository studentRepo;
    private final StaffRepository staffRepo;
    private final ParentRepository parentRepo;

    /* =========================================================
     * ================  REGISTER (USER – PUBLIC) ===============
     * ========================================================= */

    @PostMapping(value = "/auth/register/student", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerStudent(
            @RequestPart("payload") String payload,
            @RequestPart(name = "file", required = false) MultipartFile file) {

        StudentRegisterReq req;
        try {
            req = om.readValue(payload, StudentRegisterReq.class);
        } catch (Exception je) {
            return bad("Payload JSON không hợp lệ: " + je.getMessage());
        }

        try {
            if (file != null && !file.isEmpty()) {
                var saved = storage.store(file, "students");
                req.setPhotoUrl(saved.publicUrl);
            }
            authService.registerStudent(req);
            return ok(Map.of("photoUrl", req.getPhotoUrl()));
        } catch (Exception e) {
            return err("Đăng ký student thất bại: " + e.getMessage());
        }
    }

    @PostMapping(value = "/auth/register/teacher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerTeacher(
            @RequestPart("payload") String payload,
            @RequestPart(name = "file", required = false) MultipartFile file) {

        TeacherRegisterReq req;
        try {
            req = om.readValue(payload, TeacherRegisterReq.class);
        } catch (Exception je) {
            return bad("Payload JSON không hợp lệ: " + je.getMessage());
        }

        try {
            if (file != null && !file.isEmpty()) {
                var saved = storage.store(file, "teachers");
                req.setPhotoUrl(saved.publicUrl);
            }
            authService.registerTeacher(req);
            return ok(Map.of("photoUrl", req.getPhotoUrl()));
        } catch (Exception e) {
            return err("Đăng ký teacher thất bại: " + e.getMessage());
        }
    }

    @PostMapping(value = "/auth/register/parent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerParent(
            @RequestPart("payload") String payload,
            @RequestPart(name = "file", required = false) MultipartFile file) {

        ParentRegisterReq req;
        try {
            req = om.readValue(payload, ParentRegisterReq.class);
        } catch (Exception je) {
            return bad("Payload JSON không hợp lệ: " + je.getMessage());
        }

        try {
            if (file != null && !file.isEmpty()) {
                var saved = storage.store(file, "parents");
                req.setPhotoUrl(saved.publicUrl);
            }
            authService.registerParent(req);
            return ok(Map.of("photoUrl", req.getPhotoUrl()));
        } catch (Exception e) {
            return err("Đăng ký parent thất bại: " + e.getMessage());
        }
    }

    /* =========================================================
     * ====================  REGISTER (ADMIN) ===================
     * ========================================================= */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/auth/register/admin/student", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminCreateStudent(
            @RequestPart("payload") String payload,
            @RequestPart(name = "file", required = false) MultipartFile file) {

        StudentRegisterReq req;
        try { req = om.readValue(payload, StudentRegisterReq.class); }
        catch (Exception je) { return bad("Payload JSON không hợp lệ: " + je.getMessage()); }

        try {
            if (file != null && !file.isEmpty()) {
                var saved = storage.store(file, "students");
                req.setPhotoUrl(saved.publicUrl);
            }
            authService.registerStudent(req);
            return ok(Map.of("photoUrl", req.getPhotoUrl()));
        } catch (Exception e) {
            return err("ADMIN tạo student thất bại: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/auth/register/admin/teacher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminCreateTeacher(
            @RequestPart("payload") String payload,
            @RequestPart(name = "file", required = false) MultipartFile file) {

        TeacherRegisterReq req;
        try { req = om.readValue(payload, TeacherRegisterReq.class); }
        catch (Exception je) { return bad("Payload JSON không hợp lệ: " + je.getMessage()); }

        try {
            if (file != null && !file.isEmpty()) {
                var saved = storage.store(file, "teachers");
                req.setPhotoUrl(saved.publicUrl);
            }
            authService.registerTeacher(req);
            return ok(Map.of("photoUrl", req.getPhotoUrl()));
        } catch (Exception e) {
            return err("ADMIN tạo teacher thất bại: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/auth/register/admin/parent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminCreateParent(
            @RequestPart("payload") String payload,
            @RequestPart(name = "file", required = false) MultipartFile file) {

        ParentRegisterReq req;
        try { req = om.readValue(payload, ParentRegisterReq.class); }
        catch (Exception je) { return bad("Payload JSON không hợp lệ: " + je.getMessage()); }

        try {
            if (file != null && !file.isEmpty()) {
                var saved = storage.store(file, "parents");
                req.setPhotoUrl(saved.publicUrl);
            }
            authService.registerParent(req);
            return ok(Map.of("photoUrl", req.getPhotoUrl()));
        } catch (Exception e) {
            return err("ADMIN tạo parent thất bại: " + e.getMessage());
        }
    }



    /* ===================== helpers ===================== */

    private ResponseEntity<ApiResponse<Map<String, Object>>> ok(Map<String, Object> data) {
        // dùng builder hiện có trong dự án của bạn
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>build()
                .ok(data).message("OK").done());
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> bad(String msg) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, Object>>build()
                        .message(msg)
                        .done());
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> err(String msg) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.<Map<String, Object>>build()
                        .message(msg)
                        .done());
    }

}
