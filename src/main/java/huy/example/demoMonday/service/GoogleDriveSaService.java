package huy.example.demoMonday.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveSaService {

    @Value("${google.drive.service-account-key-path}")
    private String keyPath;

    @Value("${google.drive.application-name:SchoolManagement}")
    private String applicationName;

    @Value("${google.drive.upload-folder-id:}")
    private String defaultFolderId;

    // Tiêm OPTIONAL OAuth service: nếu bạn chưa tạo GoogleDriveOAuthService, bean Optional sẽ empty → không lỗi.
    private final Optional<GoogleDriveOAuthService> oauth;

    private volatile Drive cachedSaDrive;
    private final ReentrantLock lock = new ReentrantLock();

    public record UploadResult(String id, String name, String viewUrl, String downloadUrl) {}

    /** Upload “ưu tiên OAuth”, nếu không có thì fallback SA. */
    public UploadResult uploadDocxSmart(byte[] data, String fileName, String folderId) {
        // 1) OAuth trước (file thuộc My Drive của người dùng)
        if (oauth.isPresent() && oauth.get().isReady()) {
            try {
                Drive drive = oauth.get().getDriveFromStoredToken();
                return doUpload(drive, data, fileName, folderId);
            } catch (Exception e) {
                log.error("OAuth upload failed, fallback SA. Reason: {}", e.getMessage());
                // Cho phép fallback xuống SA
            }
        }
        // 2) Fallback SA (chỉ OK nếu folder thuộc Shared Drive)
        return uploadDocx(data, fileName, folderId);
    }

    /** Upload bằng Service Account (dùng cho Shared Drive). */
    public UploadResult uploadDocx(byte[] data, String fileName, String folderId) {
        try {
            if (folderId == null || folderId.isBlank()) folderId = defaultFolderId;

            File meta = new File();
            meta.setName(fileName);
            if (folderId != null && !folderId.isBlank()) {
                meta.setParents(List.of(folderId));
            }

            String mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            ByteArrayContent media = new ByteArrayContent(mime, data);

            File created = getSaDrive().files()
                    .create(meta, media)
                    .setFields("id,name,webViewLink,webContentLink,parents")
                    .setSupportsAllDrives(true)
                    .execute();

            Permission anyone = new Permission()
                    .setType("anyone")
                    .setRole("reader")
                    .setAllowFileDiscovery(false);
            getSaDrive().permissions().create(created.getId(), anyone)
                    .setSupportsAllDrives(true)
                    .execute();

            String view = "https://drive.google.com/file/d/" + created.getId() + "/view?usp=drive_link";
            String direct = "https://drive.google.com/uc?id=" + created.getId();
            log.info("Uploaded to Drive by SA: id={}, name={}, folder={}", created.getId(), created.getName(), folderId);
            return new UploadResult(created.getId(), created.getName(), view, direct);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            // Bắt trường hợp quota SA = 0 khi upload vào My Drive
            if (msg.contains("storageQuotaExceeded") || msg.contains("Service Accounts do not have storage quota")) {
                throw new RuntimeException(
                        "Service Account không có quota My Drive. " +
                                "Hãy dùng OAuth (gmail cá nhân) hoặc upload vào Shared Drive (và cấp quyền SA). " +
                                "Gợi ý: gọi /api/v1/google/drive/oauth2/init để cấp quyền OAuth rồi thử lại.",
                        e
                );
            }
            throw new RuntimeException("Upload Drive failed: " + msg, e);
        }
    }

    // Thực hiện upload (dùng chung cho OAuth/SA)
    private UploadResult doUpload(Drive drive, byte[] data, String fileName, String folderId) throws Exception {
        if (folderId == null || folderId.isBlank()) folderId = defaultFolderId;

        File meta = new File();
        meta.setName(fileName);
        if (folderId != null && !folderId.isBlank()) {
            meta.setParents(List.of(folderId));
        }

        String mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        ByteArrayContent media = new ByteArrayContent(mime, data);

        File created = drive.files()
                .create(meta, media)
                .setFields("id,name,webViewLink,webContentLink,parents")
                .setSupportsAllDrives(true)
                .execute();

        Permission anyone = new Permission()
                .setType("anyone")
                .setRole("reader")
                .setAllowFileDiscovery(false);
        drive.permissions().create(created.getId(), anyone)
                .setSupportsAllDrives(true)
                .execute();

        String view = "https://drive.google.com/file/d/" + created.getId() + "/view?usp=drive_link";
        String direct = "https://drive.google.com/uc?id=" + created.getId();
        log.info("Uploaded to Drive by OAuth: id={}, name={}, folder={}", created.getId(), created.getName(), folderId);
        return new UploadResult(created.getId(), created.getName(), view, direct);
    }

    // Khởi tạo Drive client cho Service Account (cache)
    private Drive getSaDrive() {
        if (cachedSaDrive != null) return cachedSaDrive;
        lock.lock();
        try {
            if (cachedSaDrive != null) return cachedSaDrive;

            try (InputStream in = open(keyPath)) {
                GoogleCredentials creds = ServiceAccountCredentials.fromStream(in)
                        .createScoped(Collections.singleton(DriveScopes.DRIVE));
                cachedSaDrive = new Drive.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(creds)
                ).setApplicationName(applicationName).build();
                return cachedSaDrive;
            } catch (GeneralSecurityException ge) {
                throw new RuntimeException("Init SA Drive failed (security): " + ge.getMessage(), ge);
            } catch (Exception e) {
                throw new RuntimeException("Init SA Drive failed: " + e.getMessage(), e);
            }
        } finally {
            lock.unlock();
        }
    }

    private InputStream open(String path) throws Exception {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("google.drive.service-account-key-path is empty");
        }
        if (path.startsWith("classpath:")) {
            String cp = path.substring("classpath:".length());
            return new ClassPathResource(cp).getInputStream();
        }
        return new FileInputStream(path);
    }
}
