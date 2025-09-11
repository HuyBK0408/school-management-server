-- Bật hàm tạo UUID nếu chưa có
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Hàm tính lại overall_avg cho report_card từ grade_aggregate
CREATE OR REPLACE FUNCTION recompute_report_card(p_student_id uuid, p_school_year_id uuid)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
v_overall numeric(4,1);
  v_rc_id uuid;
BEGIN
  /*
    Tính overall_avg = trung bình các grade_aggregate.avg_score
    của HS trong cùng school_year (bỏ NULL).
  */
SELECT ROUND(AVG(ga.avg_score)::numeric, 1)
INTO v_overall
FROM grade_aggregate ga
         JOIN term t ON t.id = ga.term_id
WHERE ga.student_id = p_student_id
  AND t.school_year_id = p_school_year_id
  AND ga.avg_score IS NOT NULL;

-- Tìm report_card hiện có
SELECT id
INTO v_rc_id
FROM report_card
WHERE student_id = p_student_id
  AND school_year_id = p_school_year_id
    LIMIT 1;

IF v_rc_id IS NULL THEN
    -- Chưa có: chèn bản ghi mới
    INSERT INTO report_card (id, updated_at, student_id, school_year_id, overall_avg)
    VALUES (COALESCE(gen_random_uuid(), uuid_generate_v4()), now(), p_student_id, p_school_year_id, v_overall);
ELSE
    -- Có rồi: cập nhật
UPDATE report_card
SET overall_avg = v_overall,
    updated_at = now()
WHERE id = v_rc_id;
END IF;
END;
$$;


-- 2) Trigger function chạy sau khi score_entry thay đổi
CREATE OR REPLACE FUNCTION trg_score_entry_after_change()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
v_student_id uuid;
  v_school_year_id uuid;
BEGIN
  /*
    Lấy student và school_year từ bản ghi score_entry bị ảnh hưởng:
    score_entry -> assessment -> term -> school_year
  */
SELECT se.student_id,
       t.school_year_id
INTO v_student_id, v_school_year_id
FROM score_entry se
         JOIN assessment a ON a.id = se.assessment_id
         JOIN term t       ON t.id = a.term_id
WHERE se.id = COALESCE(NEW.id, OLD.id);

-- Không có gì để làm nếu thiếu dữ liệu
IF v_student_id IS NULL OR v_school_year_id IS NULL THEN
    RETURN COALESCE(NEW, OLD);
END IF;

  -- Gọi hàm tính lại report_card
  PERFORM recompute_report_card(v_student_id, v_school_year_id);

RETURN COALESCE(NEW, OLD);
END;
$$;


-- 3) Gắn trigger vào score_entry cho 3 sự kiện: INSERT/UPDATE/DELETE
DROP TRIGGER IF EXISTS score_entry_after_change ON score_entry;
CREATE TRIGGER score_entry_after_change
    AFTER INSERT OR UPDATE OR DELETE ON score_entry
    FOR EACH ROW
    EXECUTE FUNCTION trg_score_entry_after_change();
