package huy.example.demoMonday.repository;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserPhotoRepository {
    /** Lưu ảnh cho userId, cập nhật photoUrl ở Student/Staff/Parent tương ứng, và trả về public URL. */
    String saveAndAttachToUser(UUID userId, MultipartFile file);
}
