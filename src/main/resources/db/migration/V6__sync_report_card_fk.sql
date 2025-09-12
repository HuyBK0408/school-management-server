-- đảm bảo có cột
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name='report_card' AND column_name='school_year_id'
  ) THEN
ALTER TABLE report_card ADD COLUMN school_year_id uuid;
END IF;
END $$;

-- đồng bộ FK: bỏ nếu trùng, rồi thêm lại đúng tên
ALTER TABLE report_card DROP CONSTRAINT IF EXISTS fk_report_card_schoolyear;
ALTER TABLE report_card
    ADD CONSTRAINT fk_report_card_schoolyear
        FOREIGN KEY (school_year_id) REFERENCES school_year(id);

CREATE INDEX IF NOT EXISTS idx_rc_year ON report_card(school_year_id);
