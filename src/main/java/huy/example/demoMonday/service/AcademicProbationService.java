package huy.example.demoMonday.service;

import huy.example.demoMonday.dto.request.ExpelStudentReq;
import huy.example.demoMonday.entity.GradeAggregate;
import huy.example.demoMonday.entity.Parent;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.entity.StudentParent;
import huy.example.demoMonday.entity.Term;
import huy.example.demoMonday.enums.SoftStatus;
import huy.example.demoMonday.enums.StudentStatus;
import huy.example.demoMonday.repository.GradeAggregateRepository;
import huy.example.demoMonday.repository.SchoolYearRepository;
import huy.example.demoMonday.repository.StudentParentRepository;
import huy.example.demoMonday.repository.StudentRepository;
import huy.example.demoMonday.repository.TermRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Quy tắc:
 *  - Chỉ xét kỳ chính (orderNo = 1 hoặc 2). Bỏ qua kỳ 3 (kỳ phụ).
 *  - Vi phạm: mặc định "≤ 4.0" (cấu hình app.probation.include-equals=true).
 *  - Lần 1  -> mail SV (cảnh báo)
 *  - Lần 2  -> mail PH (cảnh báo; nếu không có PH thì fallback SV)
 *  - Lần 3  -> ĐUỔI NGAY + mail "THÔNG BÁO ĐUỔI HỌC" cho SV & PH
 *  - Nếu SV đã bị đuổi trước đó -> đóng băng: không tính toán, không gửi mail (ALREADY_EXPELLED_FROZEN).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AcademicProbationService {

    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(4.0);

    /** true => ≤ 4.0 là vi phạm; false => chỉ < 4.0 mới vi phạm. */
    @Value("${app.probation.include-equals:true}")
    private boolean includeEquals;

    private final GradeAggregateRepository gradeAggRepo;
    private final StudentRepository studentRepo;
    private final StudentParentRepository studentParentRepo;
    private final TermRepository termRepo;
    private final SchoolYearRepository schoolYearRepo;
    private final StudentDeletionService deletionService;
    private final EmailService emailService;

    /** Overload gọn (không cần callback tùy biến). */
    public ProbationOutcome previewAndApply(UUID studentId, UUID termId) {
        return previewAndApply(studentId, termId, null);
    }

    /** Tính toán -> gửi mail -> nếu EXPEL thì đuổi học. */
    public ProbationOutcome previewAndApply(UUID studentId, UUID termId, MailSenderPort sender) {
        ProbationOutcome outcome = preview(studentId, termId);

        // Nếu đã đuổi rồi thì skip hoàn toàn
        if (outcome.action == Action.ALREADY_EXPELLED_FROZEN) {
            log.info("[Probation] student={} already expelled -> no mails, no mutation", studentId);
            return outcome;
        }

        // Gửi mail (sau commit)
        if (outcome.mails != null && !outcome.mails.isEmpty()) {
            for (Mail m : outcome.mails) {
                if (m.toEmail == null || m.toEmail.isBlank()) continue;
                if (sender != null) sender.send(m.toEmail, m.toName, m.subject, m.htmlBody);
                else emailService.sendHtmlAfterCommit(m.toEmail, m.subject, m.htmlBody);
            }
            log.info("[Probation] mails sent: count={} action={}", outcome.mails.size(), outcome.action);
        } else {
            log.info("[Probation] no mails to send. action={}", outcome.action);
        }

        // Đuổi học khi đạt mức EXPEL (lần 3)
        if (outcome.action == Action.EXPEL) {
            deletionService.expel(studentId, new ExpelStudentReq(
                    "Học lực " + (includeEquals ? "≤" : "<") + " 4.0 trong 3 kỳ chính liên tiếp",
                    true
            ));
            log.warn("[Probation] student={} expelled due to 3 consecutive principal terms {} 4.0",
                    studentId, includeEquals ? "<= " : "< ");
        }
        return outcome;
    }

    /** Chỉ tính toán & dựng mail (không gửi, không đuổi). */
    public ProbationOutcome preview(UUID studentId, UUID termId) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Term term = termRepo.findById(termId)
                .orElseThrow(() -> new IllegalArgumentException("Term not found: " + termId));

        // Nếu SV đã bị đuổi -> đóng băng
        if (isStudentExpelled(student)) {
            log.debug("[Probation] student={} already expelled -> freeze, skip calculation", student.getId());
            return ProbationOutcome.builder()
                    .studentId(studentId).termId(termId)
                    .termAvg(null)
                    .action(Action.ALREADY_EXPELLED_FROZEN)
                    .mails(List.of())
                    .build();
        }

        // Bỏ qua kỳ phụ
        if (!isPrincipal(term)) {
            log.debug("[Probation] skip non-principal term orderNo={}", term.getOrderNo());
            return ProbationOutcome.builder()
                    .studentId(studentId).termId(termId)
                    .termAvg(null)
                    .action(Action.SKIPPED_NON_PRINCIPAL)
                    .mails(List.of())
                    .build();
        }

        // GPA kỳ hiện tại
        BigDecimal avgRaw = computeTermAvgRaw(studentId, termId);
        if (avgRaw == null) {
            log.debug("[Probation] no GA to compute term avg yet.");
            return ProbationOutcome.builder()
                    .studentId(studentId).termId(termId)
                    .termAvg(null)
                    .action(Action.NO_DATA)
                    .mails(List.of())
                    .build();
        }
        BigDecimal avgDisplay = round1(avgRaw);

        // Không vi phạm -> reset chuỗi
        if (!isViolation(avgRaw)) {
            log.debug("[Probation] termAvg {} 4.0 => reset chain. termAvgRaw={}",
                    includeEquals ? "> " : ">= ", avgRaw);
            return ProbationOutcome.builder()
                    .studentId(studentId).termId(termId)
                    .termAvg(avgDisplay)
                    .action(Action.NO_ACTION_RESET)
                    .mails(List.of())
                    .build();
        }

        // Vi phạm kỳ hiện tại, tính chuỗi liên tiếp (1↔2, bỏ 3)
        int streak = 1;
        Optional<Term> prev1 = findPreviousPrincipalTerm(student, term);
        if (prev1.isPresent()) {
            BigDecimal avg1 = computeTermAvgRaw(studentId, prev1.get().getId());
            if (isViolation(avg1)) {
                streak = 2;
                Optional<Term> prev2 = findPreviousPrincipalTerm(student, prev1.get());
                if (prev2.isPresent()) {
                    BigDecimal avg2 = computeTermAvgRaw(studentId, prev2.get().getId());
                    if (isViolation(avg2)) {
                        streak = 3;
                    }
                }
            }
        }

        String yr = safe(() -> term.getSchoolYear().getCode());
        String termName = term.getName();
        String studentName = student.getFullName();
        String studentEmail = safe(() -> student.getUser().getEmail());
        String thresholdText = includeEquals ? "≤ 4.0" : "dưới 4.0";

        List<Mail> mails = new ArrayList<>();

        if (streak == 1) {
            mails.add(buildMailToStudent(studentName, studentEmail, avgDisplay, termName, yr, 1, thresholdText));
            return ProbationOutcome.builder()
                    .studentId(studentId).termId(termId).termAvg(avgDisplay)
                    .action(Action.WARN_STUDENT).mails(mails).build();
        }
        if (streak == 2) {
            List<String> parentTargets = findParentEmails(student.getId());
            if (parentTargets.isEmpty()) {
                mails.add(buildMailToParentFallback(studentName, studentEmail, avgDisplay, termName, yr, 2, thresholdText));
            } else {
                for (String pMail : parentTargets) {
                    mails.add(buildMailToParent(studentName, pMail, avgDisplay, termName, yr, 2, thresholdText));
                }
            }
            return ProbationOutcome.builder()
                    .studentId(studentId).termId(termId).termAvg(avgDisplay)
                    .action(Action.WARN_PARENT).mails(mails).build();
        }

        // Lần 3 -> ĐUỔI + mail THÔNG BÁO ĐUỔI cho SV & PH
        mails.add(buildExpelMailToStudent(studentName, studentEmail, avgDisplay, termName, yr, thresholdText));
        for (String pMail : findParentEmails(student.getId())) {
            mails.add(buildExpelMailToParent(studentName, pMail, avgDisplay, termName, yr, thresholdText));
        }
        return ProbationOutcome.builder()
                .studentId(studentId).termId(termId).termAvg(avgDisplay)
                .action(Action.EXPEL).mails(mails).build();
    }

    // ======================== Helpers ========================

    private boolean isPrincipal(Term term) {
        Integer o = term.getOrderNo();
        return o != null && (o == 1 || o == 2);
    }

    /** SV đã đuổi? Ưu tiên status==EXPELLED; nếu không, mọi softStatus != ACTIVE coi như đóng băng. */
    private boolean isStudentExpelled(Student s) {
        try {
            if (s.getStatus() == StudentStatus.EXPELLED) return true;
        } catch (Exception ignore) {}
        SoftStatus soft = s.getSoftStatus();
        return soft != null && soft != SoftStatus.ACTIVE;
    }

    /** Vi phạm theo cấu hình: includeEquals=true => ≤ 4.0 là vi phạm; false => chỉ < 4.0. */
    private boolean isViolation(BigDecimal avgRaw) {
        if (avgRaw == null) return false;
        int cmp = avgRaw.compareTo(THRESHOLD);
        return includeEquals ? (cmp <= 0) : (cmp < 0);
    }

    /** GPA kỳ (raw: 3 chữ số để so sánh chính xác). */
    private BigDecimal computeTermAvgRaw(UUID studentId, UUID termId) {
        List<GradeAggregate> list = gradeAggRepo.findAllByStudentAndTerm(studentId, termId);
        if (list == null || list.isEmpty()) return null;
        BigDecimal sum = BigDecimal.ZERO;
        int n = 0;
        for (GradeAggregate ga : list) {
            if (ga.getAvgScore() != null) {
                sum = sum.add(ga.getAvgScore());
                n++;
            }
        }
        if (n == 0) return null;
        return sum.divide(BigDecimal.valueOf(n), 3, RoundingMode.HALF_UP);
    }

    private BigDecimal round1(BigDecimal v) {
        return v == null ? null : v.setScale(1, RoundingMode.HALF_UP);
    }

    /** Tìm kỳ chính liền trước (2->1 cùng năm; 1->2 của năm học trước cùng trường). Bỏ kỳ 3. */
    private Optional<Term> findPreviousPrincipalTerm(Student student, Term term) {
        Integer order = term.getOrderNo();
        if (order == null || term.getSchoolYear() == null) return Optional.empty();

        UUID syId = term.getSchoolYear().getId();
        if (order == 2) {
            return termRepo.findBySchoolYearIdAndOrderNo(syId, 1);
        }
        if (order == 1) {
            if (student.getSchool() == null) return Optional.empty();
            UUID schoolId = student.getSchool().getId();
            LocalDate currentStart = term.getSchoolYear().getStartDate();
            return schoolYearRepo
                    .findTopBySchool_IdAndStartDateBeforeOrderByStartDateDesc(schoolId, currentStart)
                    .flatMap(prevYear -> termRepo.findBySchoolYearIdAndOrderNo(prevYear.getId(), 2));
        }
        return Optional.empty();
    }

    private List<String> findParentEmails(UUID studentId) {
        List<StudentParent> links = studentParentRepo.findByStudentId(studentId);
        List<String> emails = new ArrayList<>();
        for (StudentParent sp : links) {
            Parent p = sp.getParent();
            if (p == null) continue;
            if (p.getEmail() != null && !p.getEmail().isBlank()) {
                emails.add(p.getEmail());
            } else if (p.getUser() != null && p.getUser().getEmail() != null) {
                emails.add(p.getUser().getEmail());
            }
        }
        return emails;
    }

    // ====== Email templates: CẢNH BÁO (lần 1/2) ======
    private Mail buildMailToStudent(String studentName, String toEmail, BigDecimal avg,
                                    String termName, String yearCode, int strike, String thresholdText) {
        String subject = "[CẢNH BÁO HỌC LỰC] " + (yearCode!=null?yearCode+" - ":"") + termName + " (Lần " + strike + ")";
        String body = String.format("""
                <p>Chào %s,</p>
                <p>Điểm trung bình kỳ của bạn trong %s%s là <b>%s</b> (%s).</p>
                <p>Vui lòng cải thiện kết quả học tập ở kỳ tiếp theo. Nếu tiếp tục vi phạm liên tiếp, nhà trường sẽ áp dụng biện pháp kỷ luật nặng hơn.</p>
                <p>Trân trọng,</p>
                <p>Phòng Đào Tạo</p>
                """, studentName, yearCode!=null?yearCode+" - ":"", termName, avg, thresholdText);
        return new Mail(toEmail, studentName, subject, body);
    }

    private Mail buildMailToParent(String studentName, String toEmail, BigDecimal avg,
                                   String termName, String yearCode, int strike, String thresholdText) {
        String subject = "[CẢNH BÁO HỌC LỰC LẦN " + strike + "] " + studentName + " – " + (yearCode!=null?yearCode+" - ":"") + termName;
        String body = String.format("""
                <p>Kính gửi Quý Phụ huynh,</p>
                <p>Học sinh <b>%s</b> có điểm trung bình kỳ trong %s%s là <b>%s</b> (%s).</p>
                <p>Đây là lần thứ <b>%d</b> liên tiếp. Kính mong Quý Phụ huynh phối hợp cùng nhà trường để hỗ trợ em cải thiện kết quả học tập.</p>
                <p>Trân trọng,</p>
                <p>Phòng Đào Tạo</p>
                """, studentName, yearCode!=null?yearCode+" - ":"", termName, avg, thresholdText, strike);
        return new Mail(toEmail, "Phụ huynh của " + studentName, subject, body);
    }

    private Mail buildMailToParentFallback(String studentName, String toEmail, BigDecimal avg,
                                           String termName, String yearCode, int strike, String thresholdText) {
        String subject = "[CẢNH BÁO HỌC LỰC LẦN " + strike + "] " + studentName + " – " + (yearCode!=null?yearCode+" - ":"") + termName;
        String body = String.format("""
                <p>Chào %s,</p>
                <p>Hệ thống không tìm thấy email phụ huynh. Thông tin cảnh báo gửi đến bạn để chủ động thông báo với gia đình:</p>
                <p>Điểm trung bình kỳ trong %s%s: <b>%s</b> (%s). Đây là lần thứ <b>%d</b> liên tiếp.</p>
                <p>Trân trọng,</p>
                <p>Phòng Đào Tạo</p>
                """, studentName, yearCode!=null?yearCode+" - ":"", termName, avg, thresholdText, strike);
        return new Mail(toEmail, studentName, subject, body);
    }

    // ====== Email templates: ĐUỔI HỌC (lần 3) ======
    private Mail buildExpelMailToStudent(String studentName, String toEmail, BigDecimal avg,
                                         String termName, String yearCode, String thresholdText) {
        String subject = "[THÔNG BÁO ĐUỔI HỌC] " + (yearCode!=null?yearCode+" - ":"") + termName;
        String body = String.format("""
                <p>Chào %s,</p>
                <p>Căn cứ quy định học vụ, học sinh có kết quả học lực %s trong <b>3 kỳ chính liên tiếp</b> (chỉ tính kỳ 1 và 2; không tính kỳ phụ) sẽ bị <b>đuổi học</b>.</p>
                <p>Điểm trung bình kỳ hiện tại của bạn trong %s%s là <b>%s</b>.</p>
                <p>Nhà trường ra thông báo chấm dứt tư cách học sinh của bạn kể từ hôm nay. Vui lòng liên hệ Phòng Đào Tạo để hoàn tất thủ tục liên quan.</p>
                <p>Trân trọng,</p>
                <p>Phòng Đào Tạo</p>
                """, studentName, thresholdText, yearCode!=null?yearCode+" - ":"", termName, avg);
        return new Mail(toEmail, studentName, subject, body);
    }

    private Mail buildExpelMailToParent(String studentName, String toEmail, BigDecimal avg,
                                        String termName, String yearCode, String thresholdText) {
        String subject = "[THÔNG BÁO ĐUỔI HỌC] " + studentName + " – " + (yearCode!=null?yearCode+" - ":"") + termName;
        String body = String.format("""
                <p>Kính gửi Quý Phụ huynh,</p>
                <p>Căn cứ quy định học vụ, học sinh có kết quả học lực %s trong <b>3 kỳ chính liên tiếp</b> (chỉ tính kỳ 1 và 2; không tính kỳ phụ) sẽ bị <b>đuổi học</b>.</p>
                <p>Học sinh <b>%s</b> có điểm trung bình kỳ trong %s%s là <b>%s</b>. Nhà trường ra thông báo chấm dứt tư cách học sinh của em kể từ hôm nay.</p>
                <p>Kính mong Quý Phụ huynh phối hợp liên hệ Phòng Đào Tạo để hoàn tất thủ tục theo quy định.</p>
                <p>Trân trọng,</p>
                <p>Phòng Đào Tạo</p>
                """, thresholdText, studentName, yearCode!=null?yearCode+" - ":"", termName, avg);
        return new Mail(toEmail, "Phụ huynh của " + studentName, subject, body);
    }

    // ===== infra helpers =====
    private static <T> T safe(SupplierX<T> s) { try { return s.get(); } catch (Exception e) { return null; } }
    @FunctionalInterface interface SupplierX<T> { T get(); }

    // ==== Ports & DTOs ====
    public interface MailSenderPort {
        void send(String toEmail, String toName, String subject, String htmlBody);
    }

    public enum Action {
        SKIPPED_NON_PRINCIPAL, NO_DATA, NO_ACTION_RESET,
        WARN_STUDENT, WARN_PARENT, EXPEL,
        ALREADY_EXPELLED_FROZEN
    }

    @Getter
    @Builder
    public static class ProbationOutcome {
        private UUID studentId;
        private UUID termId;
        private BigDecimal termAvg; // hiển thị (đã làm tròn 1 chữ số)
        private Action action;
        @Builder.Default private List<Mail> mails = new ArrayList<>();
    }

    @Getter
    public static class Mail {
        public final String toEmail;
        public final String toName;
        public final String subject;
        public final String htmlBody;
        public Mail(String toEmail, String toName, String subject, String htmlBody) {
            this.toEmail = toEmail;
            this.toName = toName;
            this.subject = subject;
            this.htmlBody = htmlBody;
        }
    }
}
