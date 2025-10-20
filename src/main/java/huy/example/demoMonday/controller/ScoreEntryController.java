package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.request.FiveScoresBulkUpsertReq;
import huy.example.demoMonday.dto.request.FiveScoresUpsertReq;
import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.dto.response.ScoreEntryResp;
import huy.example.demoMonday.entity.ScoreEntry;
import huy.example.demoMonday.service.ScoreEntryService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;
@Getter
@Setter
@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ScoreEntryController {

    private final ScoreEntryService scoreEntryService;

    private Pageable noSort(Pageable p) {
        return PageRequest.of(p.getPageNumber(), p.getPageSize());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PostMapping
    public ResponseEntity<ApiResponse<ScoreEntryResp>> create(@RequestBody ScoreEntry body) {
        ScoreEntryResp saved = scoreEntryService.create(body); // service đang trả ScoreEntryResp
        return ResponseEntity.ok(
                ApiResponse.<ScoreEntryResp>build()
                        .ok(saved)
                        .message("Created")
                        .done()
        );
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF','PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID assessmentId,
            @RequestParam(required = false) UUID subjectId,
            @RequestParam(required = false) UUID termId,
            Pageable pageable
    ) {
        var p = noSort(pageable);
        if (studentId != null && assessmentId != null) {
            var one = scoreEntryService.getOneByStudentAssessment(studentId, assessmentId);
            return ResponseEntity.ok(
                    one.<ApiResponse<?>>map(v -> ApiResponse.<ScoreEntry>build().ok(v).done())
                            .orElseGet(() -> ApiResponse.<String>build().fail("NOT_FOUND", "Score entry not found").done())
            );
        }
        if (studentId != null)
            return ResponseEntity.ok(ApiResponse.<Object>build().ok(scoreEntryService.listByStudent(studentId, p)).done());
        if (assessmentId != null)
            return ResponseEntity.ok(ApiResponse.<Object>build().ok(scoreEntryService.listByAssessment(assessmentId, p)).done());
        if (subjectId != null)
            return ResponseEntity.ok(ApiResponse.<Object>build().ok(scoreEntryService.listBySubject(subjectId, p)).done());
        if (termId != null)
            return ResponseEntity.ok(ApiResponse.<Object>build().ok(scoreEntryService.listByTerm(termId, p)).done());
        return ResponseEntity.ok(ApiResponse.<String>build().fail("BAD_REQUEST", "Provide studentId|assessmentId|subjectId|termId or (studentId+assessmentId)").done());
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScoreEntryResp>> update(
            @PathVariable UUID id,
            @RequestParam BigDecimal score,
            @RequestParam(required = false) String note) {

        ScoreEntryResp resp = scoreEntryService.update(id, score, note);
        return ResponseEntity.ok(
                ApiResponse.<ScoreEntryResp>build().ok(resp).message("Updated").done()
        );
    }

    // ====== Nhập 1 lần 5 đầu điểm cho 1 SV–1 môn–1 kỳ ======
    @PostMapping("/upsert-five")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    public ResponseEntity<ApiResponse<Void>> upsertFive(@RequestBody @Valid FiveScoresUpsertReq req) {
        scoreEntryService.upsertFive(
                req.studentId(), req.classId(), req.subjectId(), req.termId(),
                req.quiz15(), req.quiz45(), req.assignment(), req.midterm(), req.finalExam()
        );
        return ResponseEntity.ok(ApiResponse.<Void>build().ok().message("Upserted 5 components").done());
    }

    // ====== Bulk: nhiều SV một lượt (ví dụ 20 SV) ======
    @PostMapping("/bulk-upsert-five")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    public ResponseEntity<ApiResponse<Integer>> bulkUpsertFive(@RequestBody @Valid FiveScoresBulkUpsertReq req) {
        int n = scoreEntryService.bulkUpsertFive(
                req.items().stream().map(it ->
                        new ScoreEntryService.BulkFiveItem(
                                it.studentId(), it.classId(), it.subjectId(), it.termId(),
                                it.quiz15(), it.quiz45(), it.assignment(), it.midterm(), it.finalExam()
                        )
                ).toList()
        );
        return ResponseEntity.ok(ApiResponse.<Integer>build().ok(n).message("Upserted").done());
    }
}
