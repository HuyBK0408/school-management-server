package huy.example.demoMonday.controller;

import huy.example.demoMonday.service.GoogleDriveSaService;
import huy.example.demoMonday.service.GoogleDriveSaService.UploadResult;
import huy.example.demoMonday.service.TeacherReportDocService;
import huy.example.demoMonday.service.TeacherReportDocService.DocResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/teacher/reports")
public class TeacherReportController {

    private final TeacherReportDocService docService;
    private final GoogleDriveSaService driveSaService;

    @GetMapping("/semester-average-doc")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ByteArrayResource> downloadSemesterAverageDoc(
            @RequestParam UUID classId,
            @RequestParam UUID termId
    ) {
        DocResult doc = docService.generateSemesterSummaryDoc(classId, termId);

        String safeName = toAsciiSafe(doc.fileName());
        String encoded = UriUtils.encode(safeName, StandardCharsets.UTF_8);
        ByteArrayResource body = new ByteArrayResource(doc.bytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .contentLength(doc.bytes().length)
                .body(body);
    }

    @GetMapping("/semester-average-doc-url")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> uploadSemesterAverageDocAndReturnUrl(
            @RequestParam UUID classId,
            @RequestParam UUID termId,
            @RequestParam(required = false) String folderId
    ) {
        try {
            DocResult doc = docService.generateSemesterSummaryDoc(classId, termId);
            UploadResult up = driveSaService.uploadDocxSmart(doc.bytes(), doc.fileName(), folderId);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("fileId", up.id());
            resp.put("name", up.name());
            resp.put("viewUrl", up.viewUrl());
            resp.put("downloadUrl", up.downloadUrl());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            ProblemDetail pd = ProblemDetail.forStatus(500);
            pd.setTitle("Upload Drive failed");
            pd.setDetail(e.getMessage());
            return ResponseEntity.status(500).body(pd);
        }
    }

    /** Chuẩn hoá ASCII để an toàn khi gán vào header filename */
    private static String toAsciiSafe(String s) {
        if (s == null || s.isBlank()) return "file.docx";
        String noDiacritics = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String ascii = noDiacritics.replaceAll("[^A-Za-z0-9._-]", "_");
        if (ascii.isBlank()) ascii = "file.docx";
        return ascii;
    }
}
