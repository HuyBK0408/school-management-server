// huy/example/demoMonday/controller/ProfilePhotoController.java
package huy.example.demoMonday.controller;


import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.entity.Parent;
import huy.example.demoMonday.entity.Staff;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.repository.ParentRepository;
import huy.example.demoMonday.repository.StaffRepository;
import huy.example.demoMonday.repository.StudentRepository;
import huy.example.demoMonday.repository.UserAccountRepository;
import huy.example.demoMonday.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfilePhotoController {

    private final ImageStorageService storage;
    private final UserAccountRepository userRepo;
    private final StudentRepository studentRepo;
    private final StaffRepository staffRepo;
    private final ParentRepository parentRepo;

    @PreAuthorize("hasAnyRole('STUDENT','TEACHER','PARENT','ADMIN')")
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String,String>>> updateMyPhoto(
            Authentication auth, @RequestPart("file") MultipartFile file){

        String username = auth.getName();
        var user = userRepo.findByUsernameIgnoreCase(username).orElseThrow();

        var stu = studentRepo.findByUserId(user.getId()).orElse(null);
        if (stu != null) {
            var saved = storage.store(file, "students");
            stu.setPhotoUrl(saved.publicUrl);
            studentRepo.save(stu);
            return ok(saved.publicUrl);
        }
        var stf = staffRepo.findByUserId(user.getId()).orElse(null);
        if (stf != null) {
            var saved = storage.store(file, "teachers");
            stf.setPhotoUrl(saved.publicUrl);
            staffRepo.save(stf);
            return ok(saved.publicUrl);
        }
        var par = parentRepo.findByUserId(user.getId()).orElse(null);
        if (par != null) {
            var saved = storage.store(file, "parents");
            par.setPhotoUrl(saved.publicUrl);
            parentRepo.save(par);
            return ok(saved.publicUrl);
        }
        throw new RuntimeException("Không tìm thấy hồ sơ của bạn để đổi ảnh");
    }

    private ResponseEntity<ApiResponse<Map<String,String>>> ok(String url){
        return ResponseEntity.ok(ApiResponse.<Map<String,String>>build()
                .ok(Map.of("url", url)).message("OK").done());
    }
}
