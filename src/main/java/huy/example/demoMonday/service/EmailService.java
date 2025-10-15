package huy.example.demoMonday.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@school.local}")
    private String from;

    /** Nếu true: KHÔNG gửi ra SMTP, chỉ log (dùng khi dev seed). */
    @Value("${app.mail.mock:false}")
    private boolean mock;

    /** Thêm delay để tránh rate limit (Mailtrap Sandbox thường không cần). */
    @Value("${app.bootstrap.mail-delay-ms:0}")
    private long delayMs;

    /* ===================== Public APIs ===================== */

    /** Gửi HTML: nếu có transaction thì gửi AFTER_COMMIT; nếu không thì gửi ngay. */
    public void sendHtmlAfterCommit(String to, String subject, String html) {
        Runnable task = () -> doSendHtml(from, to, subject, html);
        runNowOrAfterCommit(task);
    }

    /** Gửi TEXT: nếu có transaction thì gửi AFTER_COMMIT; nếu không thì gửi ngay. */
    public void sendTextAfterCommit(String to, String subject, String text) {
        Runnable task = () -> doSendText(from, to, subject, text);
        runNowOrAfterCommit(task);
    }

    /* --------- BACKWARD-COMPAT SHIMS (không ảnh hưởng file khác) --------- */
    /** API cũ vẫn còn ở nhiều service (ví dụ AuthService). */
    public void send(String to, String subject, String text) {
        // Giữ hành vi an toàn: gửi sau khi commit transaction (nếu có)
        sendTextAfterCommit(to, subject, text);
    }

    /** Trường hợp nơi khác từng gọi sendHtml(...). */
    public void sendHtml(String to, String subject, String html) {
        sendHtmlAfterCommit(to, subject, html);
    }

    /* ===================== Helpers ===================== */

    private void runNowOrAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { task.run(); }
            });
        } else {
            task.run();
        }
    }

    private void doSendHtml(String from, String to, String subject, String html) {
        if (mock) {
            log.info("[mail MOCK HTML] to={} | subject={} | bodyLen={}", to, subject, html == null ? 0 : html.length());
            return;
        }
        sleepIfDelayConfigured();
        try {
            MimeMessage mm = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mm, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // HTML
            mailSender.send(mm);
            log.debug("Email(HTML) sent to {} with subject '{}'", to, subject);
        } catch (Exception ex) {
            log.warn("Email(HTML) send FAILED to {} (subject='{}'): {}", to, subject, ex.getMessage());
        }
    }

    private void doSendText(String from, String to, String subject, String text) {
        if (mock) {
            log.info("[mail MOCK TEXT] to={} | subject={} | bodyLen={}", to, subject, text == null ? 0 : text.length());
            return;
        }
        sleepIfDelayConfigured();
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(text);
            mailSender.send(msg);
            log.debug("Email(TEXT) sent to {} with subject '{}'", to, subject);
        } catch (MailException ex) {
            log.warn("Email(TEXT) send FAILED to {} (subject='{}'): {}", to, subject, ex.getMessage());
        }
    }

    private void sleepIfDelayConfigured() {
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }
}
