// huy/example/demoMonday/service/ImageStorageService.java
package huy.example.demoMonday.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.upload.base-dir:uploads}")
    private String baseDir;

    @Value("${app.upload.public-prefix:/files}")
    private String publicPrefix;

    private static final Set<String> ALLOWED = Set.of("image/jpeg","image/png","image/webp","image/gif");

    public static final class StoredImage {
        public final String publicUrl;
        public final String relativePath;
        public StoredImage(String url, String rel){ this.publicUrl = url; this.relativePath = rel; }
    }

    public StoredImage store(MultipartFile file, String entityFolder) {
        if (file == null || file.isEmpty()) throw new RuntimeException("File rỗng");
        String ctype = String.valueOf(file.getContentType()).toLowerCase();
        if (!ALLOWED.contains(ctype)) throw new RuntimeException("Chỉ hỗ trợ JPEG/PNG/WebP/GIF");

        String ext = switch (ctype) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            default -> "";
        };

        var today = java.time.LocalDate.now();
        Path dir = Paths.get(baseDir, entityFolder,
                        String.valueOf(today.getYear()),
                        String.format("%02d", today.getMonthValue()))
                .toAbsolutePath();                    // ✅ tuyệt đối

        try {
            Files.createDirectories(dir);             // ✅ đảm bảo tồn tại
            String filename = java.util.UUID.randomUUID().toString().replace("-","") + ext;
            Path dest = dir.resolve(filename);

            try (var in = file.getInputStream()) {    // ✅ copy stream thay vì transferTo
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            Path rel = Paths.get(entityFolder,
                    String.valueOf(today.getYear()),
                    String.format("%02d", today.getMonthValue()),
                    filename);
            String url = publicPrefix + "/" + rel.toString().replace("\\","/");
            return new StoredImage(url, rel.toString().replace("\\","/"));
        } catch (Exception e) {
            throw new RuntimeException("Lưu ảnh thất bại: " + e.getMessage(), e);
        }
    }
}
