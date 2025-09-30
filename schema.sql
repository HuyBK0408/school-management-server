-- =========================================================
-- School Management - schema.sql (PostgreSQL)
-- Idempotent: safe to run multiple times
-- =========================================================

-- Optional: extensions (nếu muốn tự sinh uuid ở SQL)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ======================
-- CORE
-- ======================

CREATE TABLE IF NOT EXISTS public.school (
                                             id          uuid PRIMARY KEY,
                                             created_at  timestamptz(6) NOT NULL,
    updated_at  timestamptz(6) NOT NULL,
    address     varchar(255),
    email       varchar(255),
    name        varchar(255),
    phone       varchar(255)
    );

CREATE TABLE IF NOT EXISTS public.roles (
                                            id         uuid PRIMARY KEY,
                                            created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    code       varchar(255)   NOT NULL,
    CONSTRAINT uk_roles_code UNIQUE (code)
    );

CREATE TABLE IF NOT EXISTS public.user_account (
                                                   id            uuid PRIMARY KEY,
                                                   created_at    timestamptz(6) NOT NULL,
    updated_at    timestamptz(6) NOT NULL,
    email         varchar(255),
    enabled       boolean        NOT NULL DEFAULT false,            -- Đặt mặc định false an toàn hơn
    password_hash varchar(255),
    password_salt varchar(44),                                      -- Base64(32 bytes) ~ 44 chars
    phone         varchar(255),
    username      varchar(255)   NOT NULL,
    school_id     uuid,
    soft_status   varchar(20)    NOT NULL,
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT fk_user_school FOREIGN KEY (school_id) REFERENCES public.school(id),
    -- Đổi BLOCKED -> DELETED để khớp enum hiện có (nếu bạn giữ enum DELETED)
    CONSTRAINT user_soft_status_check CHECK (soft_status::text = ANY(ARRAY['ACTIVE','PENDING_DELETE','DELETED']::text[]))
    );

CREATE INDEX IF NOT EXISTS ix_user_account_username ON public.user_account (username);
CREATE INDEX IF NOT EXISTS ix_user_account_email ON public.user_account (email);

CREATE TABLE IF NOT EXISTS public.user_role (
                                                id         uuid PRIMARY KEY,
                                                created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    role_id    uuid           NOT NULL,
    user_id    uuid           NOT NULL,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_role_role  FOREIGN KEY (role_id) REFERENCES public.roles(id),
    CONSTRAINT fk_user_role_user  FOREIGN KEY (user_id) REFERENCES public.user_account(id)
    );

-- ======================
-- AUTH SUPPORT (verify/email)
-- ======================

CREATE TABLE IF NOT EXISTS public.verification_code (
                                                        id         uuid PRIMARY KEY,
                                                        created_at timestamptz(6) NOT NULL DEFAULT now(),
    updated_at timestamptz(6) NOT NULL DEFAULT now(),
    -- user_id cho phép NULL (lúc gửi mã có thể chưa có user)
    user_id    uuid,
    code       varchar(12)  NOT NULL,
    email      varchar(320) NOT NULL,
    type       varchar(20)  NOT NULL,   -- SIGNUP / RESET / CHANGE_EMAIL ...
    expires_at timestamptz(6) NOT NULL,
    used       boolean NOT NULL DEFAULT false,
    used_at    timestamptz(6),
    -- Một email chỉ có 1 mã đang hiệu lực cho mỗi loại
    CONSTRAINT uk_verification_code_email_type UNIQUE (email, type),
    CONSTRAINT fk_verification_user FOREIGN KEY (user_id) REFERENCES public.user_account(id)
    );

CREATE INDEX IF NOT EXISTS idx_verification_code_email ON public.verification_code(email);

-- ======================
-- AUTH SUPPORT (jti blacklist CHUẨN cho access token)
-- ======================

-- Thay cơ chế cũ (lưu hash toàn token) bằng blacklist theo JTI để đồng bộ với JwtService mới
CREATE TABLE IF NOT EXISTS public.token_blacklist (
                                                      id          BIGSERIAL PRIMARY KEY,
                                                      jti         varchar(64) UNIQUE NOT NULL,
    expires_at  timestamptz(6) NOT NULL
    );
CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires ON public.token_blacklist(expires_at);

-- Nếu bạn đang có bảng token_blacklist cũ (theo token_hash), có thể giữ song song một thời gian rồi drop khi không còn dùng.

-- ======================
-- AUTH SUPPORT (refresh token store)
-- ======================
CREATE TABLE IF NOT EXISTS public.refresh_token (
                                                    id          uuid PRIMARY KEY,
                                                    created_at  timestamptz(6) NOT NULL DEFAULT now(),
    updated_at  timestamptz(6) NOT NULL DEFAULT now(),
    token_hash  varchar(255)   NOT NULL,
    user_id     uuid           NOT NULL,
    expires_at  timestamptz(6) NOT NULL,
    revoked     boolean        NOT NULL DEFAULT false,
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id)
    REFERENCES public.user_account(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON public.refresh_token(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_exp  ON public.refresh_token(expires_at);

-- ======================
-- School structure
-- ======================

CREATE TABLE IF NOT EXISTS public.grade_level (
                                                  id         uuid PRIMARY KEY,
                                                  created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    name       varchar(255),
    school_id  uuid NOT NULL,
    CONSTRAINT fk_grade_level_school FOREIGN KEY (school_id) REFERENCES public.school(id)
    );

-- NOTE: giữ 1 block duy nhất
CREATE TABLE IF NOT EXISTS public.school_year (
                                                  id         uuid PRIMARY KEY,
                                                  created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    code       varchar(255),
    end_date   date,
    start_date date,
    school_id  uuid NOT NULL,
    CONSTRAINT fk_sy_school FOREIGN KEY (school_id) REFERENCES public.school(id)
    );

CREATE TABLE IF NOT EXISTS public.staff (
                                            id         uuid PRIMARY KEY,
                                            created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    dob        date,
    email      varchar(255),
    full_name  varchar(255),
    photo_url  varchar(255),
    gender     varchar(255),
    phone      varchar(255),
    "position" varchar(255),             -- SUBJECT_TEACHER / HOMEROOM_TEACHER / BOTH / OTHER
    school_id  uuid NOT NULL,
    user_id    uuid,
    CONSTRAINT uk_staff_user UNIQUE (user_id),
    CONSTRAINT fk_staff_school FOREIGN KEY (school_id) REFERENCES public.school(id),
    CONSTRAINT fk_staff_user   FOREIGN KEY (user_id)  REFERENCES public.user_account(id),
    CONSTRAINT staff_gender_check   CHECK (gender::text = ANY(ARRAY['MALE','FEMALE','OTHER']::text[])),
    CONSTRAINT staff_position_check CHECK ("position"::text = ANY(ARRAY['SUBJECT_TEACHER','HOMEROOM_TEACHER','BOTH','OTHER']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.class_room (
                                                 id                   uuid PRIMARY KEY,
                                                 created_at           timestamptz(6) NOT NULL,
    updated_at           timestamptz(6) NOT NULL,
    name                 varchar(255),
    grade_level_id       uuid NOT NULL,
    homeroom_teacher_id  uuid,
    school_id            uuid NOT NULL,
    school_year_id       uuid NOT NULL,
    CONSTRAINT fk_class_grade_level  FOREIGN KEY (grade_level_id)      REFERENCES public.grade_level(id),
    CONSTRAINT fk_class_homeroom     FOREIGN KEY (homeroom_teacher_id) REFERENCES public.staff(id),
    CONSTRAINT fk_class_school       FOREIGN KEY (school_id)           REFERENCES public.school(id),
    CONSTRAINT fk_class_year         FOREIGN KEY (school_year_id)      REFERENCES public.school_year(id)
    );

CREATE TABLE IF NOT EXISTS public.subject (
                                              id         uuid PRIMARY KEY,
                                              created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    code       varchar(255),
    name       varchar(255),
    school_id  uuid NOT NULL,
    CONSTRAINT uk_subject_code UNIQUE (code),
    CONSTRAINT fk_subject_school FOREIGN KEY (school_id) REFERENCES public.school(id)
    );

-- ======================
-- People: students & parents
-- ======================

CREATE TABLE IF NOT EXISTS public.student (
                                              id               uuid PRIMARY KEY,
                                              created_at       timestamptz(6) NOT NULL,
    updated_at       timestamptz(6) NOT NULL,
    dob              date,
    full_name        varchar(255),
    gender           varchar(255),
    photo_url        varchar(255),
    status           varchar(255),
    student_code     varchar(255),
    current_class_id uuid,
    school_id        uuid NOT NULL,
    user_id          uuid,
    CONSTRAINT uk_student_code UNIQUE (student_code),
    CONSTRAINT uk_student_user UNIQUE (user_id),
    CONSTRAINT fk_student_class  FOREIGN KEY (current_class_id) REFERENCES public.class_room(id),
    CONSTRAINT fk_student_school FOREIGN KEY (school_id)        REFERENCES public.school(id),
    CONSTRAINT fk_student_user   FOREIGN KEY (user_id)          REFERENCES public.user_account(id),
    CONSTRAINT student_gender_check CHECK (gender::text = ANY(ARRAY['MALE','FEMALE','OTHER']::text[])),
    CONSTRAINT student_status_check CHECK (status::text = ANY(ARRAY['ACTIVE','INACTIVE','TRANSFERRED']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.parent (
                                             id            uuid PRIMARY KEY,
                                             created_at    timestamptz(6) NOT NULL,
    updated_at    timestamptz(6) NOT NULL,
    address       varchar(255),
    photo_url     varchar(255),
    email         varchar(255),
    full_name     varchar(255),
    phone         varchar(255),
    relation_type varchar(255),
    user_id       uuid,
    CONSTRAINT uk_parent_user UNIQUE (user_id),
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES public.user_account(id),
    CONSTRAINT parent_relation_type_check CHECK (relation_type::text = ANY(ARRAY['FATHER','MOTHER','GUARDIAN']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.student_parent (
                                                     id         uuid PRIMARY KEY,
                                                     created_at timestamptz(6) NOT NULL,
    updated_at timestamptz(6) NOT NULL,
    parent_id  uuid NOT NULL,
    student_id uuid NOT NULL,
    CONSTRAINT uk_student_parent UNIQUE (student_id, parent_id),
    CONSTRAINT fk_sp_parent  FOREIGN KEY (parent_id)  REFERENCES public.parent(id),
    CONSTRAINT fk_sp_student FOREIGN KEY (student_id) REFERENCES public.student(id)
    );

CREATE TABLE IF NOT EXISTS public.student_card (
                                                   id           uuid PRIMARY KEY,
                                                   created_at   timestamptz(6) NOT NULL,
    updated_at   timestamptz(6) NOT NULL,
    card_number  varchar(255) NOT NULL,
    expired_date date,
    issued_date  date,
    status       varchar(255),
    student_id   uuid NOT NULL,
    CONSTRAINT uk_student_card_number UNIQUE (card_number),
    CONSTRAINT fk_student_card_student FOREIGN KEY (student_id) REFERENCES public.student(id),
    CONSTRAINT student_card_status_check CHECK (status::text = ANY(ARRAY['ACTIVE','LOST','REISSUED','EXPIRED']::text[]))
    );

-- ======================
-- Academic structure
-- ======================

CREATE TABLE IF NOT EXISTS public.term (
                                           id             uuid PRIMARY KEY,
                                           created_at     timestamptz(6) NOT NULL,
    updated_at     timestamptz(6) NOT NULL,
    name           varchar(50) NOT NULL,
    order_no       integer     NOT NULL,
    school_year_id uuid        NOT NULL,
    CONSTRAINT uq_term_name  UNIQUE (school_year_id, name),
    CONSTRAINT uq_term_order UNIQUE (school_year_id, order_no),
    CONSTRAINT fk_term_year  FOREIGN KEY (school_year_id) REFERENCES public.school_year(id)
    );

CREATE TABLE IF NOT EXISTS public.assessment (
                                                 id            uuid PRIMARY KEY,
                                                 created_at    timestamptz(6) NOT NULL,
    updated_at    timestamptz(6) NOT NULL,
    description   varchar(1000),
    exam_date     date,
    type          varchar(20) NOT NULL,
    weight        integer     NOT NULL,
    class_room_id uuid NOT NULL,
    subject_id    uuid NOT NULL,
    term_id       uuid NOT NULL,
    CONSTRAINT fk_assessment_class   FOREIGN KEY (class_room_id) REFERENCES public.class_room(id),
    CONSTRAINT fk_assessment_subject FOREIGN KEY (subject_id)   REFERENCES public.subject(id),
    CONSTRAINT fk_assessment_term    FOREIGN KEY (term_id)      REFERENCES public.term(id),
    CONSTRAINT assessment_type_check CHECK (type::text = ANY(ARRAY['QUIZ_15','QUIZ_45','ASSIGNMENT','MIDTERM','FINAL']::text[]))
    );

CREATE INDEX IF NOT EXISTS ix_assessment_group
    ON public.assessment (subject_id, class_room_id, term_id);

CREATE TABLE IF NOT EXISTS public.score_entry (
                                                  id            uuid PRIMARY KEY,
                                                  created_at    timestamptz(6) NOT NULL,
    updated_at    timestamptz(6) NOT NULL,
    note          varchar(255),
    score         numeric(4,1),
    assessment_id uuid NOT NULL,
    student_id    uuid NOT NULL,
    CONSTRAINT uk_score_unique_student_assessment UNIQUE (student_id, assessment_id),
    CONSTRAINT fk_score_assessment FOREIGN KEY (assessment_id) REFERENCES public.assessment(id),
    CONSTRAINT fk_score_student    FOREIGN KEY (student_id)    REFERENCES public.student(id)
    );

CREATE TABLE IF NOT EXISTS public.grade_aggregate (
                                                      id               uuid PRIMARY KEY,
                                                      created_at       timestamptz(6) NOT NULL,
    updated_at       timestamptz(6) NOT NULL,
    avg_score        numeric(4,1),
    conduct          varchar(255),
    parent_comment   varchar(255),
    teacher_comment  varchar(255),
    student_id       uuid NOT NULL,
    subject_id       uuid NOT NULL,
    term_id          uuid NOT NULL,
    CONSTRAINT uk_grade_agg UNIQUE (student_id, subject_id, term_id),
    CONSTRAINT fk_ga_student FOREIGN KEY (student_id) REFERENCES public.student(id),
    CONSTRAINT fk_ga_subject FOREIGN KEY (subject_id) REFERENCES public.subject(id),
    CONSTRAINT fk_ga_term    FOREIGN KEY (term_id)    REFERENCES public.term(id),
    CONSTRAINT grade_aggregate_conduct_check CHECK (conduct::text = ANY(ARRAY['GOOD','FAIR','POOR']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.lesson_journal (
                                                     id                          uuid PRIMARY KEY,
                                                     created_at                  timestamptz(6) NOT NULL,
    updated_at                  timestamptz(6) NOT NULL,
    content                     varchar(2000),
    period_no                   integer,
    teach_date                  date,
    class_id                    uuid NOT NULL,
    subject_id                  uuid NOT NULL,
    teacher_id                  uuid NOT NULL,
    transferred_from_teacher_id uuid,
    CONSTRAINT fk_lj_class      FOREIGN KEY (class_id)   REFERENCES public.class_room(id),
    CONSTRAINT fk_lj_subject    FOREIGN KEY (subject_id) REFERENCES public.subject(id),
    CONSTRAINT fk_lj_teacher    FOREIGN KEY (teacher_id) REFERENCES public.staff(id),
    CONSTRAINT fk_lj_trans_from FOREIGN KEY (transferred_from_teacher_id) REFERENCES public.staff(id)
    );

CREATE TABLE IF NOT EXISTS public.report_card (
                                                  id               uuid PRIMARY KEY,
                                                  created_at       timestamptz(6) NOT NULL,
    updated_at       timestamptz(6) NOT NULL,
    homeroom_comment varchar(255),
    overall_avg      numeric(4,1),
    overall_conduct  varchar(255),
    parent_comment   varchar(255),
    school_year_id   uuid NOT NULL,
    student_id       uuid NOT NULL,
    CONSTRAINT uk_report_card UNIQUE (student_id, school_year_id),
    CONSTRAINT fk_rc_year    FOREIGN KEY (school_year_id) REFERENCES public.school_year(id),
    CONSTRAINT fk_rc_student FOREIGN KEY (student_id)     REFERENCES public.student(id),
    CONSTRAINT report_card_overall_conduct_check CHECK (overall_conduct::text = ANY(ARRAY['GOOD','FAIR','POOR']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.teaching_assignment (
                                                          id             uuid PRIMARY KEY,
                                                          created_at     timestamptz(6) NOT NULL,
    updated_at     timestamptz(6) NOT NULL,
    assigned_from  date,
    assigned_to    date,
    class_id       uuid NOT NULL,
    school_year_id uuid NOT NULL,
    subject_id     uuid NOT NULL,
    teacher_id     uuid NOT NULL,
    CONSTRAINT uk_teaching_assignment UNIQUE (teacher_id, class_id, subject_id, school_year_id),
    CONSTRAINT fk_ta_class   FOREIGN KEY (class_id)       REFERENCES public.class_room(id),
    CONSTRAINT fk_ta_year    FOREIGN KEY (school_year_id) REFERENCES public.school_year(id),
    CONSTRAINT fk_ta_subject FOREIGN KEY (subject_id)     REFERENCES public.subject(id),
    CONSTRAINT fk_ta_teacher FOREIGN KEY (teacher_id)     REFERENCES public.staff(id)
    );

-- =========================================================
-- POST-CREATE SAFETY (Idempotent ALTERs for running on existing DB)
-- =========================================================

-- user_account: đảm bảo cột mới/unique tồn tại cho DB cũ
ALTER TABLE public.user_account
    ADD COLUMN IF NOT EXISTS password_salt varchar(44);

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_user_email') THEN
ALTER TABLE public.user_account
    ADD CONSTRAINT uk_user_email UNIQUE (email);
END IF;
END $$;

-- Index (idempotent)
CREATE INDEX IF NOT EXISTS ix_user_account_email_unique ON public.user_account (email);

-- =========================================================
-- END
-- =========================================================
