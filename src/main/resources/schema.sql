-- =========================================================
-- schema.sql  (all-in-one, idempotent)
-- =========================================================

-- Extensions (UUID)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================== CORE TABLES ==================
CREATE TABLE IF NOT EXISTS school (
                                      id UUID PRIMARY KEY,
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(100),
    email VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS roles (
                                     id UUID PRIMARY KEY,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     code VARCHAR(100) NOT NULL UNIQUE
    );

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
    CONSTRAINT fk_user_account_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

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

CREATE TABLE IF NOT EXISTS staff (
                                     id UUID PRIMARY KEY,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                     full_name VARCHAR(255),
    dob DATE,
    gender VARCHAR(50),
    phone VARCHAR(100),
    email VARCHAR(255),
    position VARCHAR(50),
    school_id UUID NOT NULL,
    user_id UUID,
    CONSTRAINT fk_staff_school FOREIGN KEY (school_id) REFERENCES school(id),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES user_account(id)
    );

CREATE TABLE IF NOT EXISTS subject (
                                       id UUID PRIMARY KEY,
                                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                       name VARCHAR(255),
    code VARCHAR(100) NOT NULL,
    school_id UUID NOT NULL,
    CONSTRAINT fk_subject_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

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

CREATE TABLE IF NOT EXISTS grade_level (
                                           id UUID PRIMARY KEY,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           name VARCHAR(50) NOT NULL,
    school_id UUID NOT NULL,
    CONSTRAINT fk_grade_level_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

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
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_student_class FOREIGN KEY (current_class_id) REFERENCES class_room(id),
    CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES school(id)
    );

CREATE TABLE IF NOT EXISTS parent (
                                      id UUID PRIMARY KEY,
                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      full_name VARCHAR(255),
    relation_type VARCHAR(50),
    phone VARCHAR(100),
    email VARCHAR(255),
    address VARCHAR(500),
    user_id UUID,
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES user_account(id)
    );

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

CREATE TABLE IF NOT EXISTS term (
                                    id UUID PRIMARY KEY,
                                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    name VARCHAR(50),
    order_no INTEGER,
    school_year_id UUID NOT NULL,
    CONSTRAINT fk_term_school_year FOREIGN KEY (school_year_id) REFERENCES school_year(id)
    );

CREATE TABLE IF NOT EXISTS teaching_assignment (
                                                   id UUID PRIMARY KEY,
                                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                   updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                                   teacher_id UUID NOT NULL,
                                                   class_id UUID NOT NULL,
                                                   subject_id UUID NOT NULL,
                                                   school_year_id UUID NOT NULL,
                                                   assigned_from DATE,
                                                   assigned_to DATE,
                                                   CONSTRAINT uq_teaching_assignment UNIQUE (teacher_id, class_id, subject_id, school_year_id),
    CONSTRAINT fk_teachassign_teacher FOREIGN KEY (teacher_id) REFERENCES staff(id),
    CONSTRAINT fk_teachassign_class   FOREIGN KEY (class_id) REFERENCES class_room(id),
    CONSTRAINT fk_teachassign_subject FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_teachassign_year    FOREIGN KEY (school_year_id) REFERENCES school_year(id)
    );

CREATE TABLE IF NOT EXISTS assessment (
                                          id UUID PRIMARY KEY,
                                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          assessment_type VARCHAR(50),
    weight INTEGER NOT NULL,
    description VARCHAR(1000),
    class_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    term_id UUID NOT NULL,
    exam_date DATE,
    CONSTRAINT fk_assessment_class   FOREIGN KEY (class_id) REFERENCES class_room(id),
    CONSTRAINT fk_assessment_subject FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_assessment_term    FOREIGN KEY (term_id) REFERENCES term(id)
    );

CREATE TABLE IF NOT EXISTS score_entry (
                                           id UUID PRIMARY KEY,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           student_id UUID NOT NULL,
                                           assessment_id UUID NOT NULL,
                                           score DOUBLE PRECISION NOT NULL,
                                           note VARCHAR(1000),
    CONSTRAINT uq_score_entry UNIQUE (student_id, assessment_id),
    CONSTRAINT fk_score_student    FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_score_assessment FOREIGN KEY (assessment_id) REFERENCES assessment(id)
    );

CREATE TABLE IF NOT EXISTS grade_aggregate (
                                               id UUID PRIMARY KEY,
                                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                               student_id UUID NOT NULL,
                                               subject_id UUID NOT NULL,
                                               term_id UUID NOT NULL,
                                               avg_score DOUBLE PRECISION,
                                               conduct VARCHAR(50),
    teacher_comment VARCHAR(2000),
    parent_comment  VARCHAR(2000),
    CONSTRAINT uq_grade_agg UNIQUE (student_id, subject_id, term_id),
    CONSTRAINT fk_gradeagg_student FOREIGN KEY (student_id) REFERENCES student(id),
    CONSTRAINT fk_gradeagg_subject FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_gradeagg_term    FOREIGN KEY (term_id) REFERENCES term(id)
    );

CREATE TABLE IF NOT EXISTS report_card (
                                           id UUID PRIMARY KEY,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           student_id UUID NOT NULL,
                                           school_year_id UUID NOT NULL,
                                           overall_avg DOUBLE PRECISION,
                                           overall_conduct VARCHAR(50),
    homeroom_comment VARCHAR(2000),
    parent_comment   VARCHAR(2000),
    CONSTRAINT uq_report_card UNIQUE (student_id, school_year_id),
    CONSTRAINT fk_reportcard_student    FOREIGN KEY (student_id)      REFERENCES student(id),
    CONSTRAINT fk_reportcard_schoolyear FOREIGN KEY (school_year_id)  REFERENCES school_year(id)
    );

CREATE TABLE IF NOT EXISTS lesson_journal (
                                              id UUID PRIMARY KEY,
                                              created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                              updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                              class_id UUID NOT NULL,
                                              subject_id UUID NOT NULL,
                                              teacher_id UUID NOT NULL,
                                              teach_date DATE,
                                              period_no INTEGER,
                                              content VARCHAR(2000),
    transferred_from_teacher_id UUID,
    CONSTRAINT fk_journal_class       FOREIGN KEY (class_id) REFERENCES class_room(id),
    CONSTRAINT fk_journal_subject     FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT fk_journal_teacher     FOREIGN KEY (teacher_id) REFERENCES staff(id),
    CONSTRAINT fk_journal_transferred FOREIGN KEY (transferred_from_teacher_id) REFERENCES staff(id)
    );

CREATE TABLE IF NOT EXISTS student_card (
                                            id UUID PRIMARY KEY,
                                            created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                            student_id UUID NOT NULL,
                                            card_number VARCHAR(100) NOT NULL UNIQUE,
    issued_date DATE,
    expired_date DATE,
    status VARCHAR(50),
    CONSTRAINT fk_student_card_student FOREIGN KEY (student_id) REFERENCES student(id)
    );

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_schoolyear_school ON school_year(school_id);
CREATE INDEX IF NOT EXISTS idx_gradelevel_school ON grade_level(school_id);
CREATE INDEX IF NOT EXISTS idx_classroom_grade   ON class_room(grade_level_id);
CREATE INDEX IF NOT EXISTS idx_classroom_year    ON class_room(school_year_id);
CREATE INDEX IF NOT EXISTS idx_classroom_school  ON class_room(school_id);
CREATE INDEX IF NOT EXISTS idx_staff_school      ON staff(school_id);
CREATE INDEX IF NOT EXISTS idx_parent_user       ON parent(user_id);
CREATE INDEX IF NOT EXISTS idx_student_school    ON student(school_id);
CREATE INDEX IF NOT EXISTS idx_student_class     ON student(current_class_id);
CREATE INDEX IF NOT EXISTS idx_term_schoolyear   ON term(school_year_id);
CREATE INDEX IF NOT EXISTS idx_assessment_cst    ON assessment(class_id, subject_id, term_id);
CREATE INDEX IF NOT EXISTS idx_score_student_assessment ON score_entry(student_id, assessment_id);
CREATE INDEX IF NOT EXISTS idx_gradeagg_sst      ON grade_aggregate(student_id, subject_id, term_id);
CREATE INDEX IF NOT EXISTS idx_journal_cst       ON lesson_journal(class_id, subject_id, teacher_id);

-- ================== COMPAT: report_card.school_year_id ==================
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'report_card' AND column_name = 'school_year_id'
  ) THEN
    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = 'report_card' AND column_name = 'schoolyear_id'
    ) THEN
      EXECUTE 'ALTER TABLE public.report_card RENAME COLUMN schoolyear_id TO school_year_id';
ELSE
      EXECUTE 'ALTER TABLE public.report_card ADD COLUMN school_year_id UUID';
END IF;
END IF;
END
$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_reportcard_schoolyear') THEN
    EXECUTE 'ALTER TABLE public.report_card
             ADD CONSTRAINT fk_reportcard_schoolyear
             FOREIGN KEY (school_year_id) REFERENCES school_year(id)';
END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_report_card_schoolyear ON public.report_card(school_year_id);

-- ================== FUNCTION & TRIGGERS ==================
CREATE OR REPLACE FUNCTION recompute_report_card(p_student_id uuid, p_school_year_id uuid)
RETURNS void
LANGUAGE plpgsql
AS $func$
DECLARE
v_overall numeric(4,1);
  v_rc_id   uuid;
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
        (SELECT gen_random_uuid() WHERE EXISTS (SELECT 1 FROM pg_extension WHERE extname='pgcrypto')),
        uuid_generate_v4()
      ),
      now(),
      now(),
      p_student_id,
      p_school_year_id,
      v_overall
    );
ELSE
UPDATE report_card
SET overall_avg = v_overall,
    updated_at  = now()
WHERE id = v_rc_id;
END IF;
END;
$func$;

CREATE OR REPLACE FUNCTION trg_score_entry_after_change()
RETURNS trigger
LANGUAGE plpgsql
AS $trg$
DECLARE
v_student_id     uuid;
  v_school_year_id uuid;
BEGIN
SELECT se.student_id, t.school_year_id
INTO v_student_id, v_school_year_id
FROM score_entry se
         JOIN assessment a ON a.id = se.assessment_id
         JOIN term t       ON t.id = a.term_id
WHERE se.id = COALESCE(NEW.id, OLD.id);

IF v_student_id IS NOT NULL AND v_school_year_id IS NOT NULL THEN
    PERFORM recompute_report_card(v_student_id, v_school_year_id);
END IF;

RETURN COALESCE(NEW, OLD);
END;
$trg$;

DROP TRIGGER IF EXISTS score_entry_after_change ON score_entry;
CREATE TRIGGER score_entry_after_change
    AFTER INSERT OR UPDATE OR DELETE ON score_entry
    FOR EACH ROW
    EXECUTE FUNCTION trg_score_entry_after_change();

-- Backfill created_at nếu còn NULL
UPDATE report_card
SET created_at = COALESCE(created_at, updated_at, now())
WHERE created_at IS NULL;

-- Defaults
ALTER TABLE assessment
    ALTER COLUMN created_at SET DEFAULT now();
