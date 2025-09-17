-- =========================================================
-- School Management - consolidated schema.sql (PostgreSQL)
-- Idempotent: safe to run multiple times
-- =========================================================

-- Optional: useful extensions for UUID if bạn muốn tự sinh UUID ở SQL
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ======================
-- CORE, least-dependent:
-- ======================

CREATE TABLE IF NOT EXISTS public.school (
                                             id          uuid PRIMARY KEY,
                                             created_at  timestamp(6) with time zone NOT NULL,
                                                                          updated_at  timestamp(6) with time zone NOT NULL,
                                                                          address     varchar(255),
    email       varchar(255),
    name        varchar(255),
    phone       varchar(255)
    );

CREATE TABLE IF NOT EXISTS public.roles (
                                            id         uuid PRIMARY KEY,
                                            created_at timestamp(6) with time zone NOT NULL,
                                                                        updated_at timestamp(6) with time zone NOT NULL,
                                                                        code       varchar(255) NOT NULL,
    CONSTRAINT uk_ch1113horj4qr56f91omojv8 UNIQUE (code)
    );

CREATE TABLE IF NOT EXISTS public.user_account (
                                                   id            uuid PRIMARY KEY,
                                                   created_at    timestamp(6) with time zone NOT NULL,
                                                                                  updated_at    timestamp(6) with time zone NOT NULL,
                                                                                  email         varchar(255),
    enabled       boolean NOT NULL,
    password_hash varchar(255),
    phone         varchar(255),
    username      varchar(255) NOT NULL,
    school_id     uuid,
    CONSTRAINT uk_castjbvpeeus0r8lbpehiu0e4 UNIQUE (username),
    CONSTRAINT fkrmitl1gl9b97ulitreqx1254i FOREIGN KEY (school_id) REFERENCES public.school(id)
    );

CREATE TABLE IF NOT EXISTS public.user_role (
                                                id         uuid PRIMARY KEY,
                                                created_at timestamp(6) with time zone NOT NULL,
                                                                            updated_at timestamp(6) with time zone NOT NULL,
                                                                            role_id    uuid NOT NULL,
                                                                            user_id    uuid NOT NULL,
                                                                            CONSTRAINT uk872xec3woupu3gw59b04pj3sa UNIQUE (user_id, role_id),
    CONSTRAINT fkt7e7djp752sqn6w22i6ocqy6q FOREIGN KEY (role_id) REFERENCES public.roles(id),
    CONSTRAINT fk7ojmv1m1vrxfl3kvt5bi5ur73 FOREIGN KEY (user_id) REFERENCES public.user_account(id)
    );

-- ======================
-- School structure:
-- ======================

CREATE TABLE IF NOT EXISTS public.grade_level (
                                                  id         uuid PRIMARY KEY,
                                                  created_at timestamp(6) with time zone NOT NULL,
                                                                              updated_at timestamp(6) with time zone NOT NULL,
                                                                              name       varchar(255),
    school_id  uuid NOT NULL,
    CONSTRAINT fkeh4pobrexqc1t21nhqodvp3um FOREIGN KEY (school_id) REFERENCES public.school(id)
    );

CREATE TABLE IF NOT EXISTS public.school_year (
                                                  id         uuid PRIMARY KEY,
                                                  created_at timestamp(6) with time zone NOT NULL,
                                                                              updated_at timestamp(6) with time zone NOT NULL,
                                                                              code       varchar(255),
    end_date   date,
    start_date date,
    school_id  uuid NOT NULL,
    CONSTRAINT fk31cpp85b4nhaotoqe9ovtomse FOREIGN KEY (school_id) REFERENCES public.school(id)
    );

CREATE TABLE IF NOT EXISTS public.staff (
                                            id         uuid PRIMARY KEY,
                                            created_at timestamp(6) with time zone NOT NULL,
                                                                        updated_at timestamp(6) with time zone NOT NULL,
                                                                        dob        date,
                                                                        email      varchar(255),
    full_name  varchar(255),
    gender     varchar(255),
    phone      varchar(255),
    "position" varchar(255),
    school_id  uuid NOT NULL,
    user_id    uuid,
    CONSTRAINT uk_7qatq4kob2sr6rlp44khhj53g UNIQUE (user_id),
    CONSTRAINT fko1sjk2xvwinvmn2gmof38cwjm FOREIGN KEY (school_id) REFERENCES public.school(id),
    CONSTRAINT fkkj3sdibk9uh8uw5puw6pkio3 FOREIGN KEY (user_id)  REFERENCES public.user_account(id),
    CONSTRAINT staff_gender_check   CHECK (gender::text   = ANY(ARRAY['MALE','FEMALE','OTHER']::text[])),
    CONSTRAINT staff_position_check CHECK ("position"::text = ANY(ARRAY['TEACHER','STAFF']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.class_room (
                                                 id                   uuid PRIMARY KEY,
                                                 created_at           timestamp(6) with time zone NOT NULL,
                                                                                       updated_at           timestamp(6) with time zone NOT NULL,
                                                                                       name                 varchar(255),
    grade_level_id       uuid NOT NULL,
    homeroom_teacher_id  uuid,
    school_id            uuid NOT NULL,
    school_year_id       uuid NOT NULL,
    CONSTRAINT fk7ge4crjuh73hs2mev5bgbbtkd FOREIGN KEY (grade_level_id)      REFERENCES public.grade_level(id),
    CONSTRAINT fkorucnerau6xxf87qh4tstcfmn FOREIGN KEY (homeroom_teacher_id) REFERENCES public.staff(id),
    CONSTRAINT fk1awc7ud7myv0u7xrywmvtufpq FOREIGN KEY (school_id)           REFERENCES public.school(id),
    CONSTRAINT fkqqradwrfqeue5n1gpu27m7v28 FOREIGN KEY (school_year_id)      REFERENCES public.school_year(id)
    );

CREATE TABLE IF NOT EXISTS public.subject (
                                              id         uuid PRIMARY KEY,
                                              created_at timestamp(6) with time zone NOT NULL,
                                                                          updated_at timestamp(6) with time zone NOT NULL,
                                                                          code       varchar(255),
    name       varchar(255),
    school_id  uuid NOT NULL,
    CONSTRAINT uk_db5g1rfeug7aywnpb6gab85ep UNIQUE (code),
    CONSTRAINT fkg9ahxfklvfv5o2d8ejweibfo8 FOREIGN KEY (school_id) REFERENCES public.school(id)
    );

-- ======================
-- People: students & parents
-- ======================

CREATE TABLE IF NOT EXISTS public.student (
                                              id              uuid PRIMARY KEY,
                                              created_at      timestamp(6) with time zone NOT NULL,
                                                                               updated_at      timestamp(6) with time zone NOT NULL,
                                                                               dob             date,
                                                                               full_name       varchar(255),
    gender          varchar(255),
    photo_url       varchar(255),
    status          varchar(255),
    student_code    varchar(255),
    current_class_id uuid,
    school_id       uuid NOT NULL,
    user_id         uuid,
    CONSTRAINT uk_pilb3uo1cimnf1sp86nqcrjsv UNIQUE (student_code),
    CONSTRAINT uk_bkix9btnoi1n917ll7bplkvg5 UNIQUE (user_id),
    CONSTRAINT fkdost0kavbxk0idia3l6imscng FOREIGN KEY (current_class_id) REFERENCES public.class_room(id),
    CONSTRAINT fk1vm0oqhk9viil6eocn49rj1l9 FOREIGN KEY (school_id)        REFERENCES public.school(id),
    CONSTRAINT fkall7qatgcsiy2fgkm0hrt2v9j FOREIGN KEY (user_id)          REFERENCES public.user_account(id),
    CONSTRAINT student_gender_check CHECK (gender::text = ANY(ARRAY['MALE','FEMALE','OTHER']::text[])),
    CONSTRAINT student_status_check CHECK (status::text = ANY(ARRAY['ACTIVE','INACTIVE','TRANSFERRED']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.parent (
                                             id           uuid PRIMARY KEY,
                                             created_at   timestamp(6) with time zone NOT NULL,
                                                                           updated_at   timestamp(6) with time zone NOT NULL,
                                                                           address      varchar(255),
    email        varchar(255),
    full_name    varchar(255),
    phone        varchar(255),
    relation_type varchar(255),
    user_id      uuid,
    CONSTRAINT uk_2er3k7pagjgspesr4bn7a2aa3 UNIQUE (user_id),
    CONSTRAINT fk3lvjknjttbn3xior57co4cfo2 FOREIGN KEY (user_id) REFERENCES public.user_account(id),
    CONSTRAINT parent_relation_type_check CHECK (relation_type::text = ANY(ARRAY['FATHER','MOTHER','GUARDIAN']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.student_parent (
                                                     id         uuid PRIMARY KEY,
                                                     created_at timestamp(6) with time zone NOT NULL,
                                                                                 updated_at timestamp(6) with time zone NOT NULL,
                                                                                 parent_id  uuid NOT NULL,
                                                                                 student_id uuid NOT NULL,
                                                                                 CONSTRAINT ukn4du8h6f5d1csx3v8pd6o7tj5 UNIQUE (student_id, parent_id),
    CONSTRAINT fk1uqsk99lie7damnsh9osouodd FOREIGN KEY (parent_id)  REFERENCES public.parent(id),
    CONSTRAINT fk3nulmrwg4cubngtp7nq5lud86 FOREIGN KEY (student_id) REFERENCES public.student(id)
    );

CREATE TABLE IF NOT EXISTS public.student_card (
                                                   id          uuid PRIMARY KEY,
                                                   created_at  timestamp(6) with time zone NOT NULL,
                                                                                updated_at  timestamp(6) with time zone NOT NULL,
                                                                                card_number varchar(255) NOT NULL,
    expired_date date,
    issued_date  date,
    status      varchar(255),
    student_id  uuid NOT NULL,
    CONSTRAINT ukf2bctn0tw8irlltamv1fv4a7x UNIQUE (card_number),
    CONSTRAINT fk22scmonccss2qkm0uk0d9qghu FOREIGN KEY (student_id) REFERENCES public.student(id),
    CONSTRAINT student_card_status_check CHECK (status::text = ANY(ARRAY['ACTIVE','LOST','REISSUED','EXPIRED']::text[]))
    );

-- ======================
-- Academic structure:
-- ======================

CREATE TABLE IF NOT EXISTS public.term (
                                           id            uuid PRIMARY KEY,
                                           created_at    timestamp(6) with time zone NOT NULL,
                                                                          updated_at    timestamp(6) with time zone NOT NULL,
                                                                          name          varchar(50) NOT NULL,
    order_no      integer NOT NULL,
    school_year_id uuid NOT NULL,
    CONSTRAINT uq_term_name  UNIQUE (school_year_id, name),
    CONSTRAINT uq_term_order UNIQUE (school_year_id, order_no),
    CONSTRAINT fkrfy727cw89qahqeeb88iyh6o1 FOREIGN KEY (school_year_id) REFERENCES public.school_year(id)
    );

CREATE TABLE IF NOT EXISTS public.assessment (
                                                 id           uuid PRIMARY KEY,
                                                 created_at   timestamp(6) with time zone NOT NULL,
                                                                               updated_at   timestamp(6) with time zone NOT NULL,
                                                                               description  varchar(1000),
    exam_date    date,
    type         varchar(20) NOT NULL,
    weight       integer NOT NULL,
    class_room_id uuid NOT NULL,
    subject_id   uuid NOT NULL,
    term_id      uuid NOT NULL,
    CONSTRAINT fk65uf6o8ocg0nls60huurwq5s8 FOREIGN KEY (class_room_id) REFERENCES public.class_room(id),
    CONSTRAINT fk2mbr7j6xr3m5pki41mp9479vi FOREIGN KEY (subject_id)   REFERENCES public.subject(id),
    CONSTRAINT fkr6lqa6vbk4ekrg3cg8frir67g FOREIGN KEY (term_id)      REFERENCES public.term(id),
    CONSTRAINT assessment_type_check CHECK (type::text = ANY(
                                            ARRAY['QUIZ_15','QUIZ_45','ASSIGNMENT','MIDTERM','FINAL']::text[]
                                                                                                           ))
    );

CREATE INDEX IF NOT EXISTS ix_assessment_group
    ON public.assessment (subject_id, class_room_id, term_id);

CREATE TABLE IF NOT EXISTS public.score_entry (
                                                  id            uuid PRIMARY KEY,
                                                  created_at    timestamp(6) with time zone NOT NULL,
                                                                                 updated_at    timestamp(6) with time zone NOT NULL,
                                                                                 note          varchar(255),
    score         numeric(4,1),
    assessment_id uuid NOT NULL,
    student_id    uuid NOT NULL,
    CONSTRAINT ukemdaq48g83s3mmaddqct56dcc UNIQUE (student_id, assessment_id),
    CONSTRAINT fkhyu40rgedflavsdx7q138j59a FOREIGN KEY (assessment_id) REFERENCES public.assessment(id),
    CONSTRAINT fkpwxpbwvgi5x0v64fiphkxgx2q FOREIGN KEY (student_id)    REFERENCES public.student(id)
    );

CREATE TABLE IF NOT EXISTS public.grade_aggregate (
                                                      id              uuid PRIMARY KEY,
                                                      created_at      timestamp(6) with time zone NOT NULL,
                                                                                       updated_at      timestamp(6) with time zone NOT NULL,
                                                                                       avg_score       numeric(4,1),
    conduct         varchar(255),
    parent_comment  varchar(255),
    teacher_comment varchar(255),
    student_id      uuid NOT NULL,
    subject_id      uuid NOT NULL,
    term_id         uuid NOT NULL,
    CONSTRAINT uk9q55xn3pk4e18uj48vnldytuw UNIQUE (student_id, subject_id, term_id),
    CONSTRAINT fkqct79uiy7bj3a0wkwo8pgbwc9 FOREIGN KEY (student_id) REFERENCES public.student(id),
    CONSTRAINT fk8us1y4mnblh44dd7to38mn10d FOREIGN KEY (subject_id) REFERENCES public.subject(id),
    CONSTRAINT fkqkbr6caap9oxi1vo4jeww9lv8 FOREIGN KEY (term_id)    REFERENCES public.term(id),
    CONSTRAINT grade_aggregate_conduct_check CHECK (conduct::text = ANY(ARRAY['GOOD','FAIR','POOR']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.lesson_journal (
                                                     id          uuid PRIMARY KEY,
                                                     created_at  timestamp(6) with time zone NOT NULL,
                                                                                  updated_at  timestamp(6) with time zone NOT NULL,
                                                                                  content     varchar(2000),
    period_no   integer,
    teach_date  date,
    class_id    uuid NOT NULL,
    subject_id  uuid NOT NULL,
    teacher_id  uuid NOT NULL,
    transferred_from_teacher_id uuid,
    CONSTRAINT fkapalxjicrbm77oqenfg9p8naq FOREIGN KEY (class_id)   REFERENCES public.class_room(id),
    CONSTRAINT fkdeg9xj5s79xlm4u0bmsl3ihal FOREIGN KEY (subject_id) REFERENCES public.subject(id),
    CONSTRAINT fk30isi2woy02rhuxd1c0vwdb4i FOREIGN KEY (teacher_id) REFERENCES public.staff(id),
    CONSTRAINT fkbkhm78nup6bk3k70xs81eln2v FOREIGN KEY (transferred_from_teacher_id) REFERENCES public.staff(id)
    );

CREATE TABLE IF NOT EXISTS public.report_card (
                                                  id               uuid PRIMARY KEY,
                                                  created_at       timestamp(6) with time zone NOT NULL,
                                                                                    updated_at       timestamp(6) with time zone NOT NULL,
                                                                                    homeroom_comment varchar(255),
    overall_avg      numeric(4,1),
    overall_conduct  varchar(255),
    parent_comment   varchar(255),
    school_year_id   uuid NOT NULL,
    student_id       uuid NOT NULL,
    CONSTRAINT uk2o1d9od0l6k16790k8f9akxjy UNIQUE (student_id, school_year_id),
    CONSTRAINT fk11wi3o7vo13egkv1b4dqrqdh FOREIGN KEY (school_year_id) REFERENCES public.school_year(id),
    CONSTRAINT fktikoyd1ptriurdbip2q8isbj FOREIGN KEY (student_id)     REFERENCES public.student(id),
    CONSTRAINT report_card_overall_conduct_check CHECK (overall_conduct::text = ANY(ARRAY['GOOD','FAIR','POOR']::text[]))
    );

CREATE TABLE IF NOT EXISTS public.teaching_assignment (
                                                          id            uuid PRIMARY KEY,
                                                          created_at    timestamp(6) with time zone NOT NULL,
                                                                                         updated_at    timestamp(6) with time zone NOT NULL,
                                                                                         assigned_from date,
                                                                                         assigned_to   date,
                                                                                         class_id      uuid NOT NULL,
                                                                                         school_year_id uuid NOT NULL,
                                                                                         subject_id    uuid NOT NULL,
                                                                                         teacher_id    uuid NOT NULL,
                                                                                         CONSTRAINT ukrd8vwnb3yq421csedfav4mftp UNIQUE (teacher_id, class_id, subject_id, school_year_id),
    CONSTRAINT fkt0fybb09d54k9vsrdepcy7qcf FOREIGN KEY (class_id)       REFERENCES public.class_room(id),
    CONSTRAINT fkataah2pqtruoegsq5e7b1w8ap FOREIGN KEY (school_year_id) REFERENCES public.school_year(id),
    CONSTRAINT fkeo8hv8bv0fc7fxbkkkej0cd26 FOREIGN KEY (subject_id)     REFERENCES public.subject(id),
    CONSTRAINT fk7lbjbnel1g0738o63l0lf0fqt FOREIGN KEY (teacher_id)     REFERENCES public.staff(id)
    );

-- (Tùy môi trường bạn có thể thêm ALTER TABLE ... OWNER TO school; nếu role 'school' tồn tại)
-- =========================================================
-- END
-- =========================================================
