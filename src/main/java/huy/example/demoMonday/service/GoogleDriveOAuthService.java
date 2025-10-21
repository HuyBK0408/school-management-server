package huy.example.demoMonday.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveOAuthService {

    @Value("${google.oauth.enabled:false}")
    private boolean enabled;

    @Value("${google.oauth.client-secret-path:}")
    private String clientSecretPath;

    @Value("${google.oauth.redirect-uri:}")
    private String redirectUri;

    private final GoogleDriveTokenStore tokenStore;

    private record ClientCfg(String clientId, String clientSecret, String tokenUri, String authUri) {}

    private ClientCfg loadClientCfg() throws Exception {
        var res = clientSecretPath.startsWith("classpath:")
                ? new ClassPathResource(clientSecretPath.substring("classpath:".length()))
                : new ClassPathResource(clientSecretPath); // đơn giản hoá demo
        try (var reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject web = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("web");
            return new ClientCfg(
                    web.get("client_id").getAsString(),
                    web.get("client_secret").getAsString(),
                    web.get("token_uri").getAsString(),
                    web.get("auth_uri").getAsString()
            );
        }
    }

    public boolean isEnabled() { return enabled; }
    public boolean isReady() { return enabled && tokenStore.hasToken(); }

    public String buildAuthorizationUrl() throws Exception {
        var cfg = loadClientCfg();
        var flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                cfg.clientId(), cfg.clientSecret(),
                List.of(DriveScopes.DRIVE_FILE) // đủ tạo & quản lý file do app tạo
        ).setAccessType("offline").build();

        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setScopes(List.of(DriveScopes.DRIVE_FILE));
        // đảm bảo lấy refresh_token nếu đã cấp trước đó
        url.set("prompt", "consent");
        return url.build();
    }

    public void handleCallback(String code) throws Exception {
        var cfg = loadClientCfg();
        TokenResponse resp = new GoogleAuthorizationCodeTokenRequest(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                cfg.tokenUri(),
                cfg.clientId(), cfg.clientSecret(), code, redirectUri
        ).execute();

        String rt = resp.getRefreshToken();
        if (rt == null || rt.isBlank()) {
            throw new IllegalStateException("Không nhận được refresh_token. Vào Google Account → Security → Third-party access, revoke app rồi chạy /init lại.");
        }
        tokenStore.saveRefreshToken(rt);
        log.info("Saved Google Drive refresh_token.");
    }

    public Drive getDriveFromStoredToken() {
        try {
            if (!isReady()) throw new IllegalStateException("Chưa có refresh_token. Hãy gọi /api/v1/google/drive/oauth2/init trước.");
            var cfg = loadClientCfg();

            UserCredentials userCreds = UserCredentials.newBuilder()
                    .setClientId(cfg.clientId())
                    .setClientSecret(cfg.clientSecret())
                    .setRefreshToken(tokenStore.getRefreshToken())
                    .build();

            return new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(userCreds)
            ).setApplicationName("SchoolManagement").build();

        } catch (GeneralSecurityException ge) {
            throw new RuntimeException("Init OAuth Drive failed (security): " + ge.getMessage(), ge);
        } catch (Exception e) {
            throw new RuntimeException("Init OAuth Drive failed: " + e.getMessage(), e);
        }
    }
}
