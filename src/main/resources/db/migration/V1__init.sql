-- ============================
--  UUID-based schema for School MS
--  Compatible with PostgreSQL & H2 (MODE=PostgreSQL)
-- ============================

-- 1) Core: school
CREATE TABLE IF NOT EXISTS school (
                                      id UUID PRIMARY KEY,
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(100),
    email VARCHAR(255)
    );

-- 2) Role (explicit table name "roles")
CREATE TABLE IF NOT EXISTS roles (
                                     id UUID PRIMARY KEY,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     code VARCHAR(100) NOT NULL UNIQUE
    );

-- 3) user_account
CREATE TABLE IF NOT EXISTS user_account (
                                            id UUID PRIMARY KEY,
                                            created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            username VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(100),
    enabled BOOLEAN NOT NULL,
    school_id UUID,
    CONSTRAINT fk_user_account_school
    FOREIGN KEY (school_id) REFERENCES school(id)
    );

-- 4) user_role
CREATE TABLE IF NOT EXISTS user_role (
                                         id UUID PRIMARY KEY,
                                         created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                         updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                         user_id UUID NOT NULL,
                                         role_id UUID NOT NULL,
                                         CONSTRAINT uq_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES user_account(id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(id)
    );

-- 5) staff
CREATE TABLE IF NOT EXISTS staff (
                                     id UUID PRIMARY KEY,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     full_name VARCHAR(255),
    dob DATE,
    gender VARCHAR(50),
    phone VARCHAR(100),
    email VARCHAR(255),
    position VARCHAR(50), -- TEACHER, STAFF
    school_id UUID NOT NULL,
    user_id UUID,
    CONSTRAINT fk_staff_school FOREIGN KEY (school_id) REFERENCES school(id),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES user_account(id)
    );

-- 6) subject
CREATE TABLE IF NOT EXISTS subject (
                                       id UUID PRIMARY KEY,
                                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                       name VARCHAR(255),
    code VARCHAR(100) NOT NULL,
    school_id UUID NOT NULL,
    CONSTRAINT fk_subject_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

-- 7) school_year
CREATE TABLE IF NOT EXISTS school_year (
                                           id UUID PRIMARY KEY,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           code VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    school_id UUID NOT NULL,
    CONSTRAINT fk_school_year_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

-- 8) grade_level
CREATE TABLE IF NOT EXISTS grade_level (
                                           id UUID PRIMARY KEY,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           name VARCHAR(50) NOT NULL,
    school_id UUID NOT NULL,
    CONSTRAINT fk_grade_level_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

-- 9) class_room
CREATE TABLE IF NOT EXISTS class_room (
                                          id UUID PRIMARY KEY,
                                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          name VARCHAR(100),
    grade_level_id UUID NOT NULL,
    school_year_id UUID NOT NULL,
    school_id UUID NOT NULL,
    homeroom_teacher_id UUID,
    CONSTRAINT fk_class_room_grade_level FOREIGN KEY (grade_level_id) REFERENCES grade_level(id),
    CONSTRAINT fk_class_room_school_year FOREIGN KEY (school_year_id) REFERENCES school_year(id),
    CONSTRAINT fk_class_room_school FOREIGN KEY (school_id) REFERENCES school(id),
    CONSTRAINT fk_class_room_homeroom FOREIGN KEY (homeroom_teacher_id) REFERENCES staff(id)
    );

-- 10) student
CREATE TABLE IF NOT EXISTS student (
                                       id UUID PRIMARY KEY,
                                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                       full_name VARCHAR(255),
    dob DATE,
    gender VARCHAR(50),
    student_code VARCHAR(100) NOT NULL UNIQUE,
    current_class_id UUID,
    school_id UUID NOT NULL,
    photo_url VARCHAR(1024),
    status VARCHAR(50) NOT NULL, -- ACTIVE/INACTIVE/TRANSFERRED
    CONSTRAINT fk_student_class FOREIGN KEY (current_class_id) REFERENCES class_room(id),
    CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

-- 11) parent
CREATE TABLE IF NOT EXISTS parent (
                                      id UUID PRIMARY KEY,
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      full_name VARCHAR(255),
    relation_type VARCHAR(50), -- FATHER/MOTHER/GUARDIAN
    phone VARCHAR(100),
    email VARCHAR(255),
    address VARCHAR(500),
    user_id UUID,
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES user_account(id)
    );

-- 12) student_parent
CREATE TABLE IF NOT EXISTS student_parent (
                                              id UUID PRIMARY KEY,
                                              created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                              updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                              student_id UUID NOT NULL,
                                              parent_id UUID NOT NULL,
                                              CONSTRAINT uq_student_parent UNIQUE (student_id, parent_id),
    CONSTRAINT fk_student_parent_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_student_parent_parent FOREIGN KEY (parent_id) REFERENCES parent(id)
    );

-- 13) term
CREATE TABLE IF NOT EXISTS term (
                                    id UUID PRIMARY KEY,
                                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    name VARCHAR(50),
    order_no INTEGER,
    school_year_id UUID NOT NULL,
    CONSTRAINT fk_term_school_year FOREIGN KEY (school_year_id) REFERENCES school_year(id)
    );

-- 14) teaching_assignment
CREATE TABLE IF NOT EXISTS teaching_assignment (
                                                   id UUID PRIMARY KEY,
                                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                   updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                   teacher_id UUID NOT NULL,      -- -> staff
                                                   class_id UUID NOT NULL,        -- -> class_room
                                                   subject_id UUID NOT NULL,      -- -> subject
                                                   school_year_id UUID NOT NULL,  -- -> school_year
                                                   assigned_from DATE,
                                                   assigned_to DATE,
                                                   CONSTRAINT uq_teaching_assignment UNIQUE (teacher_id, class_id, subject_id, school_year_id),
    CONSTRAINT fk_teachassign_teacher FOREIGN KEY (teacher_id) REFERENCES staff(id),
    CONSTRAINT fk_teachassign_class FOREIGN KEY (class_id) REFERENCES class_room(id),
    CONSTRAINT fk_teachassign_subject FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_teachassign_school_year FOREIGN KEY (school_year_id) REFERENCES school_year(id)
    );

-- 15) assessment
CREATE TABLE IF NOT EXISTS assessment (
                                          id UUID PRIMARY KEY,
                                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          assessment_type VARCHAR(50),   -- QUIZ_15, QUIZ_45, MIDTERM, FINAL, ASSIGNMENT
    weight INTEGER NOT NULL,
    description VARCHAR(1000),
    class_id UUID NOT NULL,        -- -> class_room
    subject_id UUID NOT NULL,      -- -> subject
    term_id UUID NOT NULL,         -- -> term
    exam_date DATE,
    CONSTRAINT fk_assessment_class FOREIGN KEY (class_id) REFERENCES class_room(id),
    CONSTRAINT fk_assessment_subject FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_assessment_term FOREIGN KEY (term_id) REFERENCES term(id)
    );

-- 16) score_entry
CREATE TABLE IF NOT EXISTS score_entry (
                                           id UUID PRIMARY KEY,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           student_id UUID NOT NULL,
                                           assessment_id UUID NOT NULL,
                                           score DOUBLE PRECISION NOT NULL,
                                           note VARCHAR(1000),
    CONSTRAINT uq_score_entry UNIQUE (student_id, assessment_id),
    CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_score_assessment FOREIGN KEY (assessment_id) REFERENCES assessment(id)
    );

-- 17) grade_aggregate
CREATE TABLE IF NOT EXISTS grade_aggregate (
                                               id UUID PRIMARY KEY,
                                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                               student_id UUID NOT NULL,
                                               subject_id UUID NOT NULL,
                                               term_id UUID NOT NULL,
                                               avg_score DOUBLE PRECISION,
                                               conduct VARCHAR(50),           -- GOOD/FAIR/POOR
    teacher_comment VARCHAR(2000),
    parent_comment VARCHAR(2000),
    CONSTRAINT uq_grade_agg UNIQUE (student_id, subject_id, term_id),
    CONSTRAINT fk_gradeagg_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_gradeagg_subject FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_gradeagg_term FOREIGN KEY (term_id) REFERENCES term(id)
    );

-- 18) report_card
CREATE TABLE IF NOT EXISTS report_card (
                                           id UUID PRIMARY KEY,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           student_id UUID NOT NULL,
                                           school_year_id UUID NOT NULL,
                                           overall_avg DOUBLE PRECISION,
                                           overall_conduct VARCHAR(50),
    homeroom_comment VARCHAR(2000),
    parent_comment VARCHAR(2000),
    CONSTRAINT uq_report_card UNIQUE (student_id, school_year_id),
    CONSTRAINT fk_reportcard_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_reportcard_schoolyear FOREIGN KEY (school_year_id) REFERENCES school_year(id)
    );

-- 19) lesson_journal
CREATE TABLE IF NOT EXISTS lesson_journal (
                                              id UUID PRIMARY KEY,
                                              created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                              updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                              class_id UUID NOT NULL,        -- -> class_room
                                              subject_id UUID NOT NULL,      -- -> subject
                                              teacher_id UUID NOT NULL,      -- -> staff
                                              teach_date DATE,
                                              period_no INTEGER,
                                              content VARCHAR(2000),
    transferred_from_teacher_id UUID,
    CONSTRAINT fk_journal_class FOREIGN KEY (class_id) REFERENCES class_room(id),
    CONSTRAINT fk_journal_subject FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_journal_teacher FOREIGN KEY (teacher_id) REFERENCES staff(id),
    CONSTRAINT fk_journal_transferred FOREIGN KEY (transferred_from_teacher_id) REFERENCES staff(id)
    );

-- 20) student_card
CREATE TABLE IF NOT EXISTS student_card (
                                            id UUID PRIMARY KEY,
                                            created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            student_id UUID NOT NULL,
                                            card_number VARCHAR(100) NOT NULL UNIQUE,
    issued_date DATE,
    expired_date DATE,
    status VARCHAR(50),            -- ACTIVE/LOST/REISSUED/EXPIRED
    CONSTRAINT fk_student_card_student FOREIGN KEY (student_id) REFERENCES student(id)
    );

-- ============================
-- Helpful indexes (match JPA @Index hints)
-- ============================
CREATE INDEX IF NOT EXISTS idx_schoolyear_school ON school_year(school_id);
CREATE INDEX IF NOT EXISTS idx_gradelevel_school ON grade_level(school_id);
CREATE INDEX IF NOT EXISTS idx_classroom_grade ON class_room(grade_level_id);
CREATE INDEX IF NOT EXISTS idx_classroom_year ON class_room(school_year_id);
CREATE INDEX IF NOT EXISTS idx_classroom_school ON class_room(school_id);
CREATE INDEX IF NOT EXISTS idx_staff_school ON staff(school_id);
CREATE INDEX IF NOT EXISTS idx_parent_user ON parent(user_id);
CREATE INDEX IF NOT EXISTS idx_student_school ON student(school_id);
CREATE INDEX IF NOT EXISTS idx_student_class ON student(current_class_id);
CREATE INDEX IF NOT EXISTS idx_term_schoolyear ON term(school_year_id);
CREATE INDEX IF NOT EXISTS idx_assessment_cst ON assessment(class_id, subject_id, term_id);
CREATE INDEX IF NOT EXISTS idx_score_student_assessment ON score_entry(student_id, assessment_id);
CREATE INDEX IF NOT EXISTS idx_gradeagg_sst ON grade_aggregate(student_id, subject_id, term_id);
CREATE INDEX IF NOT EXISTS idx_journal_cst ON lesson_journal(class_id, subject_id, teacher_id);
