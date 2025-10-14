package huy.example.demoMonday.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repo hợp nhất (không gọi service):
 * - Tự lưu file (java.nio.file)
 * - Dùng EntityManager để UPDATE photoUrl theo userId ở Student/Staff/Parent.
 */
@Repository
@RequiredArgsConstructor
public class UserPhotoJpaRepository implements UserPhotoRepository {

    @PersistenceContext
    private final EntityManager em;

    @Value("${app.upload.base-dir:uploads}")
    private String baseDir;                     // ví dụ: uploads
    @Value("${app.upload.public-prefix:/files}")
    private String publicPrefix;                // ví dụ: /files

    @Override
    @Transactional
    public String saveAndAttachToUser(UUID userId, MultipartFile file) {
        validate(file);

        // 1) Lưu file ra filesystem -> lấy public URL
        String url = storeToFs(userId, file);

        // 2) Cập nhật vào entity nào đang gắn với userId (Student/Staff/Parent)
        int updated = 0;

        updated += em.createQuery("""
                update Student s set s.photoUrl = :url
                where s.user.id = :uid
            """).setParameter("url", url)
                .setParameter("uid", userId)
                .executeUpdate();

        if (updated == 0) {
            updated += em.createQuery("""
                update Staff st set st.photoUrl = :url
                where st.user.id = :uid
            """).setParameter("url", url)
                    .setParameter("uid", userId)
                    .executeUpdate();
        }

        if (updated == 0) {
            updated += em.createQuery("""
                update Parent p set p.photoUrl = :url
                where p.user.id = :uid
            """).setParameter("url", url)
                    .setParameter("uid", userId)
                    .executeUpdate();
        }

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy profile để gắn ảnh");
        }

        return url;
    }

    /* ============ helpers ============ */

    private void validate(MultipartFile f){
        if (f == null || f.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        if (f.getSize() > 5_000_000L)
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Max 5MB");
        var ct = Optional.ofNullable(f.getContentType()).orElse("");
        if (!(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp")))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPEG/PNG/WEBP allowed");
    }

    private String storeToFs(UUID userId, MultipartFile file){
        String yyyy = String.valueOf(LocalDate.now().getYear());
        String mm   = String.format("%02d", LocalDate.now().getMonthValue());
        String ext  = resolveExt(file.getOriginalFilename());

        Path folder = Paths.get(baseDir, "users", userId.toString(), yyyy, mm);
        try {
            Files.createDirectories(folder);
            String filename = UUID.randomUUID() + ext;
            Path dest = folder.resolve(filename);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            String rel = String.join("/", "users", userId.toString(), yyyy, mm, filename).replace("\\", "/");
            return (publicPrefix + "/" + rel).replace("//", "/");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
        }
    }

    private String resolveExt(String name){
        if (name == null) return ".jpg";
        int i = name.lastIndexOf('.');
        String ext = (i >= 0 ? name.substring(i).toLowerCase() : ".jpg");
        if (!ext.matches("\\.(jpg|jpeg|png|webp)$")) return ".jpg";
        return ext;
    }
}
