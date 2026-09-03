CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','RECRUITER','INTERVIEWER')),
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE job_openings (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    department  VARCHAR(255),
    status      VARCHAR(20) NOT NULL DEFAULT 'OPEN'
                CHECK (status IN ('OPEN','ON_HOLD','CLOSED')),
    created_by  BIGINT NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE candidates (
    id               BIGSERIAL PRIMARY KEY,
    full_name        VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    phone            VARCHAR(30),
    experience_years NUMERIC(4,1),
    job_id           BIGINT NOT NULL REFERENCES job_openings(id),
    status           VARCHAR(30) NOT NULL DEFAULT 'APPLIED'
                     CHECK (status IN ('APPLIED','SHORTLISTED','INTERVIEW_SCHEDULED','SELECTED','REJECTED')),
    added_by         BIGINT NOT NULL REFERENCES users(id),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE candidate_documents (
    id           BIGSERIAL PRIMARY KEY,
    candidate_id BIGINT NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    file_name    VARCHAR(255) NOT NULL,
    file_path    VARCHAR(500) NOT NULL,
    uploaded_by  BIGINT NOT NULL REFERENCES users(id),
    uploaded_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE interviews (
    id               BIGSERIAL PRIMARY KEY,
    candidate_id     BIGINT NOT NULL REFERENCES candidates(id),
    job_id           BIGINT NOT NULL REFERENCES job_openings(id),
    interviewer_id   BIGINT NOT NULL REFERENCES users(id),
    scheduled_at     TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 45,
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                     CHECK (status IN ('SCHEDULED','RESCHEDULED','CANCELLED','COMPLETED')),
    created_by       BIGINT NOT NULL REFERENCES users(id),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interviews_interviewer ON interviews(interviewer_id, scheduled_at);
CREATE INDEX idx_candidates_job ON candidates(job_id);

CREATE TABLE interview_feedback (
    id                   BIGSERIAL PRIMARY KEY,
    interview_id         BIGINT NOT NULL UNIQUE REFERENCES interviews(id),
    technical_rating     INT NOT NULL CHECK (technical_rating BETWEEN 1 AND 5),
    communication_rating INT NOT NULL CHECK (communication_rating BETWEEN 1 AND 5),
    recommendation       VARCHAR(10) NOT NULL CHECK (recommendation IN ('SELECT','REJECT','HOLD')),
    comments             TEXT,
    submitted_by         BIGINT NOT NULL REFERENCES users(id),
    submitted_at         TIMESTAMP NOT NULL DEFAULT NOW()
);