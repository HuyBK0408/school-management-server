package huy.example.demoMonday.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from:no-reply@school.local}")
    private String from;

    /** Bật khi chạy seed/bootstrap/dev để không gửi mail thật. */
    @Value("${app.mail.suppress:false}")
    private boolean suppress;

    /** Thêm delay giữa các email để tránh rate-limit Mailtrap (ms). */
    @Value("${app.bootstrap.mail-delay-ms:0}")
    private long delayMs;

    /**
     * Gửi email an toàn:
     * - Nếu đang có transaction: đăng ký gửi ở phase AFTER_COMMIT ⇒ rollback sẽ KHÔNG gửi.
     * - Nếu không có transaction: gửi ngay.
     * - Nếu suppress=true: chỉ log, không gửi.
     * - Nếu lỗi SMTP: chỉ warn, KHÔNG ném exception ra ngoài.
     */
    public void send(String to, String subject, String text) {
        SimpleMailMessage msg = build(to, subject, text);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            // Chỉ gửi sau khi commit thành công
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    doSend(msg);
                }
            });
        } else {
            // Không có TX thì gửi luôn
            doSend(msg);
        }
    }

    /* ============ helpers ============ */

    private SimpleMailMessage build(String to, String subject, String text) {
        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        return msg;
    }

    private void doSend(SimpleMailMessage msg) {
        if (suppress) {
            log.info("[mail suppressed] to={} | subject={} | bodyLen={}",
                    String.join(",", msg.getTo()), msg.getSubject(),
                    msg.getText() == null ? 0 : msg.getText().length());
            return;
        }

        // Thêm delay nếu cấu hình
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            mailSender.send(msg);
            log.debug("Email sent to {} with subject '{}'", String.join(",", msg.getTo()), msg.getSubject());
        } catch (MailException ex) {
            // Ví dụ: 550 Too many emails per second...
            log.warn("Email send FAILED to {} (subject='{}'): {}",
                    String.join(",", msg.getTo()), msg.getSubject(), ex.getMessage());
        }
    }
}
