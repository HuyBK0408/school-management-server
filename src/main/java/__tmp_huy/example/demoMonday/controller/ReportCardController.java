package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.service.ReportCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/report-cards")
@RequiredArgsConstructor
public class ReportCardController {

    private final ReportCardService reportCardService;
    private Pageable noSort(Pageable p) { return PageRequest.of(p.getPageNumber(), p.getPageSize()); }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) UUID termId,
            @RequestParam(required = false) UUID schoolYearId,
            @RequestParam(required = false) UUID schoolId,
            Pageable pageable
    ) {
        var p = noSort(pageable);
        if (studentId != null)    return ResponseEntity.ok(ApiResponse.<Object>build().ok(reportCardService.listByStudent(studentId, p)).done());
        if (schoolYearId != null) return ResponseEntity.ok(ApiResponse.<Object>build().ok(reportCardService.listBySchoolYear(schoolYearId, p)).done());
        if (schoolId != null)     return ResponseEntity.ok(ApiResponse.<Object>build().ok(reportCardService.listBySchool(schoolId, p)).done());
        if (subjectId != null)    return ResponseEntity.ok(ApiResponse.<Object>build().ok(reportCardService.listBySubject(subjectId, p)).done());
        if (termId != null)       return ResponseEntity.ok(ApiResponse.<Object>build().ok(reportCardService.listByTerm(termId, p)).done());

        return ResponseEntity.ok(ApiResponse.<String>build().fail("BAD_REQUEST",
                "Use: studentId | subjectId | termId | schoolYearId | schoolId").done());
    }
}
