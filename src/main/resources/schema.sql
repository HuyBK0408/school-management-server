-- =========================
-- School Management Schema
-- PostgreSQL (no CREATE SCHEMA here)
-- =========================

-- 1) Core reference tables
CREATE TABLE IF NOT EXISTS school (
                                      id   UUID PRIMARY KEY,
                                      name VARCHAR(200) NOT NULL
    );

CREATE TABLE IF NOT EXISTS grade_level (
                                           id        UUID PRIMARY KEY,
                                           name      VARCHAR(50) NOT NULL,
    school_id UUID NOT NULL REFERENCES school(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS school_year (
                                           id        UUID PRIMARY KEY,
                                           year      INTEGER NOT NULL,
                                           school_id UUID NOT NULL REFERENCES school(id) ON DELETE CASCADE,
    CONSTRAINT uq_school_year UNIQUE (school_id, year)
    );

CREATE TABLE IF NOT EXISTS staff (
                                     id        UUID PRIMARY KEY,
                                     full_name VARCHAR(150) NOT NULL,
    email     VARCHAR(200) UNIQUE
    );

CREATE TABLE IF NOT EXISTS class_room (
                                          id                  UUID PRIMARY KEY,
                                          name                VARCHAR(50) NOT NULL,
    school_year_id      UUID NOT NULL REFERENCES school_year(id) ON DELETE CASCADE,
    grade_level_id      UUID NOT NULL REFERENCES grade_level(id) ON DELETE RESTRICT,
    homeroom_teacher_id UUID REFERENCES staff(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS subject (
                                       id        UUID PRIMARY KEY,
                                       code      VARCHAR(50)  NOT NULL,
    name      VARCHAR(150) NOT NULL,
    school_id UUID NOT NULL REFERENCES school(id) ON DELETE CASCADE,
    CONSTRAINT uq_subject UNIQUE (school_id, code)
    );

CREATE TABLE IF NOT EXISTS student (
                                       id            UUID PRIMARY KEY,
                                       full_name     VARCHAR(150) NOT NULL,
    class_room_id UUID REFERENCES class_room(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS parent (
                                      id        UUID PRIMARY KEY,
                                      full_name VARCHAR(150) NOT NULL,
    phone     VARCHAR(30)
    );

CREATE TABLE IF NOT EXISTS student_parent (
                                              student_id UUID NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    parent_id  UUID NOT NULL REFERENCES parent(id)  ON DELETE CASCADE,
    relation   VARCHAR(50) NOT NULL,
    PRIMARY KEY (student_id, parent_id)
    );

-- 2) Terms (max 3 / school_year) – KHÔNG trigger
CREATE TABLE IF NOT EXISTS term (
                                    id             UUID PRIMARY KEY,
                                    name           VARCHAR(50) NOT NULL,
    order_no       INTEGER NOT NULL,
    school_year_id UUID NOT NULL REFERENCES school_year(id) ON DELETE CASCADE,
    CONSTRAINT uq_term_order UNIQUE (school_year_id, order_no),
    CONSTRAINT uq_term_name  UNIQUE (school_year_id, name),
    CONSTRAINT chk_term_order CHECK (order_no BETWEEN 1 AND 3)
    );

-- 3) Teaching & lessons
CREATE TABLE IF NOT EXISTS teaching_assignment (
                                                   id            UUID PRIMARY KEY,
                                                   staff_id      UUID NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    subject_id    UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    class_room_id UUID NOT NULL REFERENCES class_room(id) ON DELETE CASCADE,
    term_id       UUID NOT NULL REFERENCES term(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS lesson_journal (
                                              id            UUID PRIMARY KEY,
                                              class_room_id UUID NOT NULL REFERENCES class_room(id) ON DELETE CASCADE,
    subject_id    UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    staff_id      UUID REFERENCES staff(id) ON DELETE SET NULL,  -- KHÔNG NOT NULL
    term_id       UUID NOT NULL REFERENCES term(id) ON DELETE CASCADE,
    lesson_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    content       TEXT
    );

-- 4) Assessment – dùng VARCHAR thay ENUM
-- type: 'QUIZ_15' | 'QUIZ_45' | 'ASSIGNMENT' | 'MIDTERM' | 'FINAL'
CREATE TABLE IF NOT EXISTS assessment (
                                          id            UUID PRIMARY KEY,
                                          title         VARCHAR(150) NOT NULL,
    weight        INTEGER NOT NULL DEFAULT 1,
    type          VARCHAR(20)  NOT NULL,
    subject_id    UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    class_room_id UUID NOT NULL REFERENCES class_room(id) ON DELETE CASCADE,
    term_id       UUID NOT NULL REFERENCES term(id) ON DELETE CASCADE,
    description   VARCHAR(1000),
    exam_date     DATE
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_ass_midterm
    ON assessment (class_room_id, subject_id, term_id)
    WHERE type = 'MIDTERM';

CREATE UNIQUE INDEX IF NOT EXISTS ux_ass_final
    ON assessment (class_room_id, subject_id, term_id)
    WHERE type = 'FINAL';

-- 5) Scores & reports
CREATE TABLE IF NOT EXISTS score_entry (
                                           id            UUID PRIMARY KEY,
                                           student_id    UUID NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    assessment_id UUID NOT NULL REFERENCES assessment(id) ON DELETE CASCADE,
    score         NUMERIC(5,2) NOT NULL CHECK (score BETWEEN 0 AND 10),
    note          VARCHAR(500),
    CONSTRAINT uq_score_student_ass UNIQUE (student_id, assessment_id)
    );

CREATE TABLE IF NOT EXISTS grade_aggregate (
                                               id         UUID PRIMARY KEY,
                                               student_id UUID NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subject(id) ON DELETE CASCADE,
    term_id    UUID NOT NULL REFERENCES term(id) ON DELETE CASCADE,
    avg_score  NUMERIC(5,2),
    conduct    VARCHAR(50),
    CONSTRAINT uq_grade_agg UNIQUE (student_id, subject_id, term_id)
    );

CREATE TABLE IF NOT EXISTS student_card (
                                            id             UUID PRIMARY KEY,
                                            student_id     UUID NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    school_year_id UUID NOT NULL REFERENCES school_year(id) ON DELETE CASCADE,
    card_no        VARCHAR(50),
    CONSTRAINT uq_student_card UNIQUE (student_id, school_year_id)
    );

CREATE TABLE IF NOT EXISTS report_card (
                                           id             UUID PRIMARY KEY,
                                           student_id     UUID NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    school_year_id UUID NOT NULL REFERENCES school_year(id) ON DELETE CASCADE,
    year_gpa       NUMERIC(5,2),
    conduct        VARCHAR(50),
    CONSTRAINT uq_report_card UNIQUE (student_id, school_year_id)
    );

-- 6) Auth
CREATE TABLE IF NOT EXISTS role (
                                    id   UUID PRIMARY KEY,
                                    name VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS user_account (
                                            id            UUID PRIMARY KEY,
                                            username      VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    staff_id      UUID REFERENCES staff(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS user_role (
                                         user_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role(id)        ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
    );

-- 7) Helpful indexes
CREATE INDEX IF NOT EXISTS ix_student_class       ON student(class_room_id);
CREATE INDEX IF NOT EXISTS ix_teach_assign        ON teaching_assignment(staff_id, subject_id, class_room_id, term_id);
CREATE INDEX IF NOT EXISTS ix_assessment_group    ON assessment(subject_id, class_room_id, term_id);
CREATE INDEX IF NOT EXISTS ix_score_entry_student ON score_entry(student_id);
CREATE INDEX IF NOT EXISTS ix_grade_agg_student   ON grade_aggregate(student_id);
CREATE INDEX IF NOT EXISTS ix_student_card_sy     ON student_card(school_year_id);
CREATE INDEX IF NOT EXISTS ix_report_card_sy      ON report_card(school_year_id);
