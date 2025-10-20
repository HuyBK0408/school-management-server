package huy.example.demoMonday.controller;

import huy.example.demoMonday.service.TeacherReportDocService;
import huy.example.demoMonday.service.TeacherReportDocService.DocResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teacher/reports")
public class TeacherReportController {

    private final TeacherReportDocService teacherReportDocService;

    @GetMapping("/semester-average-doc")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ByteArrayResource> downloadSemesterAverageDoc(
            @RequestParam UUID classId,
            @RequestParam UUID termId
    ) {
        DocResult doc = teacherReportDocService.generateSemesterSummaryDoc(classId, termId);
        ByteArrayResource resource = new ByteArrayResource(doc.bytes());

        // File name gốc (có dấu) & bản ASCII fallback
        String utf8FileName = doc.fileName();
        String asciiFileName = toAsciiSafe(utf8FileName); // chỉ ASCII để Tomcat không chặn

        // RFC 5987: filename*=UTF-8''<percent-encoded>
        String encodedUtf8 = UriUtils.encode(utf8FileName, StandardCharsets.UTF_8);

        String contentDisposition =
                "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedUtf8;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(doc.bytes().length)
                .body(resource);
    }

    /** Chuẩn hoá “không dấu, ASCII-only” để an toàn khi gán vào header filename */
    private static String toAsciiSafe(String s) {
        if (s == null || s.isBlank()) return "file.docx";
        // loại dấu (NFD), bỏ ký tự tổ hợp, sau đó thay ký tự không an toàn bằng '_'
        String noDiacritics = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        // chỉ giữ chữ, số, gạch dưới, gạch ngang, chấm. Các ký tự khác thay bằng '_'
        String ascii = noDiacritics.replaceAll("[^A-Za-z0-9._-]", "_");
        // tránh rỗng
        if (ascii.isBlank()) ascii = "file.docx";
        return ascii;
    }
}
