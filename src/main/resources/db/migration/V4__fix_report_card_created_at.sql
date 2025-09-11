-- 1) Sửa hàm, thêm created_at khi INSERT
CREATE OR REPLACE FUNCTION recompute_report_card(p_student_id uuid, p_school_year_id uuid)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
v_overall numeric(4,1);
  v_rc_id uuid;
BEGIN
SELECT ROUND(AVG(ga.avg_score)::numeric, 1)
INTO v_overall
FROM grade_aggregate ga
         JOIN term t ON t.id = ga.term_id
WHERE ga.student_id = p_student_id
  AND t.school_year_id = p_school_year_id
  AND ga.avg_score IS NOT NULL;

SELECT id
INTO v_rc_id
FROM report_card
WHERE student_id = p_student_id
  AND school_year_id = p_school_year_id
    LIMIT 1;

IF v_rc_id IS NULL THEN
    INSERT INTO report_card (id, created_at, updated_at, student_id, school_year_id, overall_avg)
    VALUES (
      COALESCE(
        CASE WHEN EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pgcrypto') THEN gen_random_uuid() END,
        uuid_generate_v4()
      ),
      now(),               -- <= thêm created_at
      now(),
      p_student_id,
      p_school_year_id,
      v_overall
    );
ELSE
UPDATE report_card
SET overall_avg = v_overall,
    updated_at = now()
WHERE id = v_rc_id;
END IF;
END;
$$;

-- 2) (Tuỳ chọn nhưng nên có) backfill các bản ghi cũ còn NULL created_at
UPDATE report_card
SET created_at = COALESCE(created_at, updated_at, now())
WHERE created_at IS NULL;
