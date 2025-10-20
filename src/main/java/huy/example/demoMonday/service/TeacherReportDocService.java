package huy.example.demoMonday.service;

import huy.example.demoMonday.entity.ClassRoom;
import huy.example.demoMonday.entity.SchoolYear;
import huy.example.demoMonday.entity.Student;
import huy.example.demoMonday.entity.Term;
import huy.example.demoMonday.enums.SoftStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TeacherReportDocService {

    private final EntityManager em;

    /** Kết quả tạo file: bytes + tên file để controller set Content-Disposition */
    public record DocResult(byte[] bytes, String fileName) {}

    @Transactional(readOnly = true)
    public DocResult generateSemesterSummaryDoc(UUID classId, UUID termId) {
        // 1) Load class & term
        ClassRoom clazz = em.find(ClassRoom.class, classId);
        if (clazz == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found");

        Term term = em.find(Term.class, termId);
        if (term == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Term not found");

        SchoolYear schoolYearFromTerm = term.getSchoolYear();
        if (schoolYearFromTerm == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Term missing SchoolYear");

        // 2) Lấy HS ACTIVE (không ORDER BY ở DB để tránh UnknownPathException do khác schema)
        List<Student> students = em.createQuery("""
            select s from Student s
            where s.currentClass.id = :c
              and (s.softStatus is null or s.softStatus = :active)
        """, Student.class)
                .setParameter("c", classId)
                .setParameter("active", SoftStatus.ACTIVE)
                .getResultList();

        // 2b) Sắp xếp bằng Java theo tên hiển thị (bất chấp schema)
        students.sort(
                Comparator.comparing((Student s) -> resolveStudentName(s).toLowerCase(Locale.ROOT))
                        .thenComparing(Student::getId)
        );

        // 3) Tên hiển thị
        String className = resolveClassName(clazz);
        String termName  = resolveTermName(term);

        // Lấy năm học: thử từ Term.schoolYear; nếu không ra, fallback từ ClassRoom.schoolYear
        String yearName  = resolveSchoolYearName(schoolYearFromTerm);
        if (isBlankOrDefault(yearName)) {
            try {
                Object classYearObj = invokeGetter(clazz, "getSchoolYear");
                if (classYearObj instanceof SchoolYear sy) {
                    String alt = resolveSchoolYearName(sy);
                    if (!isBlankOrDefault(alt)) yearName = alt;
                } else if (classYearObj != null) {
                    String alt = resolveYearNameFromObject(classYearObj);
                    if (!isBlankOrDefault(alt)) yearName = alt;
                }
            } catch (Exception ignore) {}
        }
        if (isBlankOrDefault(yearName)) yearName = "N/A";

        // 4) Tính TB học kỳ cho từng SV
        List<RowData> rows = new ArrayList<>();
        int stt = 1;
        for (Student s : students) {
            BigDecimal semAvg = computeSemesterAverage(s.getId(), termId);
            rows.add(new RowData(stt++, s, className, yearName, semAvg, classify(semAvg)));
        }

        // 5) Title
        String title = String.format(
                "Thống kê điểm trung bình lớp %s trong học kỳ %s năm học %s",
                className, termName, yearName
        );

        // 6) Tạo DOCX ra file tạm -> đọc bytes -> xoá file tạm
        try {
            Path tmp = createDocx(title, rows);
            byte[] data;
            try {
                data = Files.readAllBytes(tmp);
            } finally {
                try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
            }
            String fileName = String.format(
                    "Thong_ke_TB_%s_%s_%s.docx",
                    className.replaceAll("\\s+", "_"),
                    termName.replaceAll("\\s+", "_"),
                    LocalDateTime.now().toString().replace(':','-')
            );
            return new DocResult(data, fileName);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to build DOCX: " + e.getMessage(), e);
        }
    }

    /** Trung bình cộng các GradeAggregate.avgScore (NULL bỏ qua), scale 2 */
    private BigDecimal computeSemesterAverage(UUID studentId, UUID termId) {
        TypedQuery<BigDecimal> q = em.createQuery("""
            select ga.avgScore
            from GradeAggregate ga
            where ga.student.id = :s
              and ga.term.id = :t
              and ga.avgScore is not null
        """, BigDecimal.class);
        q.setParameter("s", studentId);
        q.setParameter("t", termId);

        var list = q.getResultList();
        if (list.isEmpty()) return null;

        BigDecimal sum = BigDecimal.ZERO;
        int n = 0;
        for (BigDecimal a : list) {
            if (a != null) { sum = sum.add(a); n++; }
        }
        if (n == 0) return null;
        return sum.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
    }

    /** Xếp loại: <5 Kém, 5–7.9 Khá, ≥8 Tốt; null → "-" */
    private String classify(BigDecimal avg) {
        if (avg == null) return "-";
        if (avg.compareTo(BigDecimal.valueOf(5.0)) < 0) return "Kém";
        if (avg.compareTo(BigDecimal.valueOf(8.0)) >= 0) return "Tốt";
        return "Khá";
    }

    // --------------------------
    //  Helpers: tên hiển thị
    // --------------------------

    private String resolveStudentName(Student s) {
        if (s == null) return "N/A";
        try { var m = s.getClass().getMethod("getFullName"); var v = m.invoke(s); if (v != null) return v.toString(); } catch (Exception ignore) {}
        try { var m = s.getClass().getMethod("getName");     var v = m.invoke(s); if (v != null) return v.toString(); } catch (Exception ignore) {}
        try {
            var m1 = s.getClass().getMethod("getLastName");
            var m2 = s.getClass().getMethod("getFirstName");
            Object ln = m1.invoke(s), fn = m2.invoke(s);
            if (ln != null || fn != null) {
                String joined = ((ln == null ? "" : ln.toString()) + " " + (fn == null ? "" : fn.toString())).trim();
                if (!joined.isBlank()) return joined;
            }
        } catch (Exception ignore) {}
        try { var m = s.getClass().getMethod("getCode"); var v = m.invoke(s); if (v != null) return v.toString(); } catch (Exception ignore) {}
        return "N/A";
    }

    private String resolveClassName(ClassRoom c) {
        if (c == null) return "N/A";
        try { var m = c.getClass().getMethod("getName"); var v = m.invoke(c); if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Exception ignore) {}
        try { var m = c.getClass().getMethod("getClassName"); var v = m.invoke(c); if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Exception ignore) {}
        return "N/A";
    }

    private String resolveTermName(Term t) {
        if (t == null) return "XYZ";
        try { var m = t.getClass().getMethod("getName"); var v = m.invoke(t); if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Exception ignore) {}
        try { var m = t.getClass().getMethod("getTermName"); var v = m.invoke(t); if (v != null && !v.toString().isBlank()) return v.toString(); } catch (Exception ignore) {}
        return "XYZ";
    }

    /** Trích tên năm học từ SchoolYear (rất “bền” với nhiều schema khác nhau) */
    private String resolveSchoolYearName(SchoolYear y) {
        if (y == null) return "20xx";

        // 1) Thử các getter String phổ biến theo thứ tự ưu tiên
        String s = firstNonBlankFromGetters(y,
                "getName", "getYearName", "getLabel", "getDisplayName", "getTitle", "getAcademicYear", "getSchoolYear", "getYear"
        );
        if (!isBlankOrDefault(s)) {
            String normalized = s.trim();
            if (looksLikeYearLabel(normalized)) return normalized;
        }

        // 2) Thử các cặp numeric năm: start/end, from/to, begin/finish, startYear/endYear, ...
        String pair = firstYearPair(y,
                new String[][]{
                        {"getStartYear","getEndYear"},
                        {"getFromYear","getToYear"},
                        {"getBeginYear","getFinishYear"},
                        {"getStart","getEnd"},
                        {"getFrom","getTo"}
                }
        );
        if (!isBlankOrDefault(pair)) return pair;

        // 3) Quét toàn bộ getter trả String -> chọn cái nào nhìn như nhãn năm (chứa 4 số, có/không có dấu '-')
        String scan = scanAnyStringGetterForYearLabel(y);
        if (!isBlankOrDefault(scan)) return scan;

        // 4) Cuối cùng: nếu toString() không phải dạng Object@hash, dùng luôn
        String ts = y.toString();
        if (ts != null && !ts.isBlank() && !ts.matches(".+@\\p{XDigit}+")) return ts;

        return "20xx";
    }

    // -------- generic resolve from arbitrary object (fallback từ ClassRoom.getSchoolYear())
    private String resolveYearNameFromObject(Object obj) {
        if (obj == null) return null;
        // dùng chính thuật toán ở trên (nhưng cho Object bất kỳ)
        String s = firstNonBlankFromGetters(obj,
                "getName", "getYearName", "getLabel", "getDisplayName", "getTitle", "getAcademicYear", "getSchoolYear", "getYear"
        );
        if (!isBlankOrDefault(s) && looksLikeYearLabel(s)) return s.trim();

        String pair = firstYearPair(obj,
                new String[][]{
                        {"getStartYear","getEndYear"},
                        {"getFromYear","getToYear"},
                        {"getBeginYear","getFinishYear"},
                        {"getStart","getEnd"},
                        {"getFrom","getTo"}
                }
        );
        if (!isBlankOrDefault(pair)) return pair;

        String scan = scanAnyStringGetterForYearLabel(obj);
        if (!isBlankOrDefault(scan)) return scan;

        String ts = obj.toString();
        if (ts != null && !ts.isBlank() && !ts.matches(".+@\\p{XDigit}+")) return ts;
        return null;
    }

    // --------------------------
    //  Tạo file DOCX
    // --------------------------

    private record RowData(int index, Student s, String className, String yearName, BigDecimal avg, String result) {}

    /** Tạo file DOCX theo mẫu yêu cầu, trả về đường dẫn file tạm */
    private Path createDocx(String title, List<RowData> rows) {
        try (XWPFDocument doc = new XWPFDocument()) {
            // Title
            XWPFParagraph pTitle = doc.createParagraph();
            pTitle.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun rTitle = pTitle.createRun();
            rTitle.setText(title);
            rTitle.setBold(true);
            rTitle.setFontSize(14);

            // spacing
            doc.createParagraph().createRun().addBreak();

            // Table: header + rows
            int cols = 6;
            XWPFTable table = doc.createTable(Math.max(1, rows.size() + 1), cols);

            setCell(table.getRow(0).getCell(0), "STT", true);
            setCell(table.getRow(0).getCell(1), "Tên SV", true);
            setCell(table.getRow(0).getCell(2), "Lớp", true);
            setCell(table.getRow(0).getCell(3), "Năm học", true);
            setCell(table.getRow(0).getCell(4), "Điểm TB", true);
            setCell(table.getRow(0).getCell(5), "Kết quả", true);

            int r = 1;
            for (RowData rd : rows) {
                var row = (r < table.getNumberOfRows()) ? table.getRow(r) : table.createRow();
                setCell(row.getCell(0), String.valueOf(rd.index()), false);
                setCell(row.getCell(1), resolveStudentName(rd.s()), false);
                setCell(row.getCell(2), rd.className(), false);
                setCell(row.getCell(3), rd.yearName(), false);
                setCell(row.getCell(4), rd.avg() == null ? "-" : rd.avg().toPlainString(), false);
                setCell(row.getCell(5), rd.result(), false);
                r++;
            }

            // spacing
            doc.createParagraph().createRun().addBreak();

            // Footer
            XWPFParagraph pFooter = doc.createParagraph();
            pFooter.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun rFooter = pFooter.createRun();
            rFooter.setFontSize(12);
            rFooter.setText("Ký tên: ___________________");

            // write to tmp file
            Path tmp = Files.createTempFile("semester-average-", ".docx");
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                doc.write(bos);
                Files.write(tmp, bos.toByteArray());
            }
            return tmp;
        } catch (Exception e) {
            throw new RuntimeException("Create DOCX failed: " + e.getMessage(), e);
        }
    }

    private void setCell(XWPFTableCell cell, String text, boolean header) {
        if (cell.getParagraphs() != null && !cell.getParagraphs().isEmpty()) {
            cell.removeParagraph(0);
        }
        var p = cell.addParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        var r = p.createRun();
        r.setText(text == null ? "" : text);
        if (header) r.setBold(true);
    }

    // --------------------------
    //  Reflection helpers
    // --------------------------

    private static boolean isBlankOrDefault(String s) {
        if (s == null) return true;
        String t = s.trim();
        return t.isEmpty() || "20xx".equalsIgnoreCase(t) || "N/A".equalsIgnoreCase(t);
    }

    private static String firstNonBlankFromGetters(Object obj, String... getterNames) {
        for (String g : getterNames) {
            try {
                Object v = invokeGetter(obj, g);
                if (v != null) {
                    String s = v.toString().trim();
                    if (!s.isEmpty()) return s;
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    private static Object invokeGetter(Object obj, String getter) throws Exception {
        Method m = obj.getClass().getMethod(getter);
        return m.invoke(obj);
    }

    // pattern: chứa 4 số hoặc "yyyy-yyyy" / "yyyy – yyyy"
    private static final Pattern YEAR_LABEL =
            Pattern.compile(".*\\b(19|20)\\d{2}(\\s*[-–]\\s*(19|20)\\d{2})?.*", Pattern.UNICODE_CASE);

    private static boolean looksLikeYearLabel(String s) {
        return s != null && YEAR_LABEL.matcher(s).matches();
    }

    private static String firstYearPair(Object obj, String[][] pairs) {
        for (String[] pair : pairs) {
            try {
                Object a = invokeGetter(obj, pair[0]);
                Object b = invokeGetter(obj, pair[1]);
                if (a != null && b != null) {
                    String sa = a.toString().trim();
                    String sb = b.toString().trim();
                    if (!sa.isEmpty() && !sb.isEmpty()) {
                        String label = sa + " - " + sb;
                        if (looksLikeYearLabel(label)) return label;
                    }
                }
            } catch (Exception ignore) {}
        }
        return null;
    }

    private static String scanAnyStringGetterForYearLabel(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getParameterCount() == 0
                    && m.getName().startsWith("get")
                    && m.getReturnType() == String.class) {
                try {
                    Object v = m.invoke(obj);
                    if (v != null) {
                        String s = v.toString().trim();
                        if (!s.isEmpty() && looksLikeYearLabel(s)) {
                            return s;
                        }
                    }
                } catch (Exception ignore) {}
            }
        }
        return null;
    }
}
