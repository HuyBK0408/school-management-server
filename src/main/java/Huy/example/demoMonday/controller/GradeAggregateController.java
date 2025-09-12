package huy.example.demoMonday.controller;

import huy.example.demoMonday.dto.response.ApiResponse;
import huy.example.demoMonday.dto.response.GradeAggregateResp;
import huy.example.demoMonday.service.GradeAggregateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/grade-aggregates")
@RequiredArgsConstructor
public class GradeAggregateController {

    private final GradeAggregateService gradeAggregateService;

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

        if (studentId != null && subjectId != null && termId != null && schoolYearId != null) {
            if (!gradeAggregateService.termBelongsToSchoolYear(termId, schoolYearId)) {
                return ResponseEntity.ok(ApiResponse.<String>build()
                        .fail("BAD_REQUEST","termId does not belong to schoolYearId").done());
            }
            var one = gradeAggregateService.getOneByStudentSubjectTerm(studentId, subjectId, termId);
            return ResponseEntity.ok(
                    one.<ApiResponse<?>>map(v -> ApiResponse.<GradeAggregateResp>build().ok(v).done())
                            .orElseGet(() -> ApiResponse.<String>build().fail("NOT_FOUND","Grade aggregate not found").done())
            );
        }
        if (studentId != null && subjectId != null && termId != null) {
            var one = gradeAggregateService.getOneByStudentSubjectTerm(studentId, subjectId, termId);
            return ResponseEntity.ok(
                    one.<ApiResponse<?>>map(v -> ApiResponse.<GradeAggregateResp>build().ok(v).done())
                            .orElseGet(() -> ApiResponse.<String>build().fail("NOT_FOUND","Grade aggregate not found").done())
            );
        }
        if (studentId != null && schoolYearId != null)
            return ResponseEntity.ok(ApiResponse.<Object>build()
                    .ok(gradeAggregateService.listByStudentSchoolYear(studentId, schoolYearId, p)).done());
        if (subjectId != null && schoolYearId != null)
            return ResponseEntity.ok(ApiResponse.<Object>build()
                    .ok(gradeAggregateService.listBySubjectSchoolYear(subjectId, schoolYearId, p)).done());

        if (studentId != null)    return ResponseEntity.ok(ApiResponse.<Object>build().ok(gradeAggregateService.listByStudent(studentId, p)).done());
        if (subjectId != null)    return ResponseEntity.ok(ApiResponse.<Object>build().ok(gradeAggregateService.listBySubject(subjectId, p)).done());
        if (termId != null)       return ResponseEntity.ok(ApiResponse.<Object>build().ok(gradeAggregateService.listByTerm(termId, p)).done());
        if (schoolYearId != null) return ResponseEntity.ok(ApiResponse.<Object>build().ok(gradeAggregateService.listBySchoolYear(schoolYearId, p)).done());
        if (schoolId != null)     return ResponseEntity.ok(ApiResponse.<Object>build().ok(gradeAggregateService.listBySchool(schoolId, p)).done());

        return ResponseEntity.ok(ApiResponse.<String>build().fail("BAD_REQUEST",
                "Use: studentId | subjectId | termId | schoolYearId | schoolId | (studentId+subjectId+termId) | (studentId+schoolYearId) | (subjectId+schoolYearId)"
        ).done());
    }
    //================== HẾT: GET LINH HOẠT ====================

    // Giữ nguyên logic cũ: cập nhật comment/conduct
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','STAFF')")
    @PatchMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<GradeAggregateResp>> updateComments(
            @PathVariable UUID id,
            @RequestParam(required = false) huy.example.demoMonday.enums.Conduct conduct,
            @RequestParam(required = false) String teacherComment,
            @RequestParam(required = false) String parentComment
    ) {
        var resp = gradeAggregateService.updateComments(id, conduct, teacherComment, parentComment);
        return ResponseEntity.ok(ApiResponse.<GradeAggregateResp>build().ok(resp).message("Updated").done());
    }
}