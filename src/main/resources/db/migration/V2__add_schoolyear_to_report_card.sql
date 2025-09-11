-- Thêm cột (cho phép null để không vỡ dữ liệu cũ)
ALTER TABLE report_card
    ADD COLUMN IF NOT EXISTS schoolyear_id UUID;

-- Tạo FK
ALTER TABLE report_card
    ADD CONSTRAINT fk_report_card_schoolyear
        FOREIGN KEY (schoolyear_id) REFERENCES school_year(id);

-- (Tùy chọn) Tạo index để query nhanh
CREATE INDEX IF NOT EXISTS idx_report_card_schoolyear
    ON report_card (schoolyear_id);
