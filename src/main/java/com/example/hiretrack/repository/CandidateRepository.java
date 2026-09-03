package com.example.hiretrack.repository;


import com.example.hiretrack.dto.CandidateResponse;
import com.example.hiretrack.enums.CandidateStatus;
import com.example.hiretrack.jooq.tables.records.CandidatesRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.hiretrack.jooq.tables.CandidateDocuments.CANDIDATE_DOCUMENTS;
import static com.example.hiretrack.jooq.tables.Candidates.CANDIDATES;
import static com.example.hiretrack.jooq.tables.JobOpenings.JOB_OPENINGS;
import static com.example.hiretrack.jooq.tables.Users.USERS;

@Repository
public class CandidateRepository {
    private final DSLContext dsl;

    public CandidateRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public CandidatesRecord create(String fullName, String email, String phone, BigDecimal experienceYears, Long jobId, Long addedBy){
        return dsl.insertInto(CANDIDATES)
                .set(CANDIDATES.FULL_NAME,fullName)
                .set(CANDIDATES.EMAIL,email)
                .set(CANDIDATES.PHONE,phone)
                .set(CANDIDATES.EXPERIENCE_YEARS,experienceYears)
                .set(CANDIDATES.JOB_ID,jobId)
                .set(CANDIDATES.ADDED_BY,addedBy)
                .set(CANDIDATES.CREATED_AT,LocalDateTime.now())
                .returning()
                .fetchOne();
    }

    public Optional<CandidatesRecord> findById(Long id){
        return Optional.ofNullable(
                dsl.selectFrom(CANDIDATES)
                        .where(CANDIDATES.ID.eq(id))
                        .fetchOne()
        );
    }

    public Optional<CandidateResponse> findByIdWithDetails(Long id){
        return Optional.ofNullable(
                dsl.select(CANDIDATES.asterisk(),USERS.FULL_NAME.as("added_by_name"),JOB_OPENINGS.TITLE.as("job_title"),
                        CANDIDATE_DOCUMENTS.FILE_NAME.as("resume_file_name"))
                        .from(CANDIDATES)
                        .leftJoin(USERS).on(CANDIDATES.ADDED_BY.eq(USERS.ID))
                        .leftJoin(JOB_OPENINGS).on(CANDIDATES.JOB_ID.eq(JOB_OPENINGS.ID))
                        .leftJoin(CANDIDATE_DOCUMENTS).on(CANDIDATES.ID.eq(CANDIDATE_DOCUMENTS.CANDIDATE_ID))
                        .where(CANDIDATES.ID.eq(id))
                        .fetchOne(r->{
                            CandidateResponse response = new CandidateResponse();
                            response.setId(r.get(CANDIDATES.ID));
                            response.setFullName(r.get(CANDIDATES.FULL_NAME));
                            response.setEmail(r.get(CANDIDATES.EMAIL));
                            response.setPhone(r.get(CANDIDATES.PHONE));
                            response.setExperienceYears(r.get(CANDIDATES.EXPERIENCE_YEARS));
                            response.setJobId(r.get(CANDIDATES.JOB_ID));
                            response.setJobTitle(r.get("job_title", String.class));
                            response.setStatus(CandidateStatus.valueOf(r.get(CANDIDATES.STATUS)));
                            response.setResumeFileName(r.get("resume_file_name", String.class));
                            response.setAddedById(r.get(CANDIDATES.ADDED_BY));
                            response.setAddedByName(r.get("added_by_name", String.class));
                            response.setCreatedAt(r.get(CANDIDATES.CREATED_AT));
                            response.setUpdatedAt(r.get(CANDIDATES.UPDATED_AT));
                            return response;
                        })
        );
    }

    public void update(Long id,String fullName,String email,String phone,BigDecimal experienceYears ){
         dsl.update(CANDIDATES)
                .set(CANDIDATES.FULL_NAME,fullName)
                .set(CANDIDATES.EMAIL,email)
                .set(CANDIDATES.PHONE,phone)
                .set(CANDIDATES.EXPERIENCE_YEARS,experienceYears)
                .set(CANDIDATES.UPDATED_AT,LocalDateTime.now())
                .where(CANDIDATES.ID.eq(id))
                .execute();
    }

    public void updateStatus(Long id,CandidateStatus status){
        dsl.update(CANDIDATES)
                .set(CANDIDATES.STATUS,status.name())
                .set(CANDIDATES.UPDATED_AT,LocalDateTime.now())
                .execute();
    }

    public void addResume(Long candidateId, String fileName, String filePath, Long uploadedBy) {
        Long count = dsl.selectCount()
                .from(CANDIDATE_DOCUMENTS)
                .where(CANDIDATE_DOCUMENTS.CANDIDATE_ID.eq(candidateId))
                .fetchOne(0, Long.class);

        if (count > 0) {
            dsl.update(CANDIDATE_DOCUMENTS)
                    .set(CANDIDATE_DOCUMENTS.FILE_NAME, fileName)
                    .set(CANDIDATE_DOCUMENTS.FILE_PATH, filePath)
                    .set(CANDIDATE_DOCUMENTS.UPLOADED_BY, uploadedBy)
                    .set(CANDIDATE_DOCUMENTS.UPLOADED_AT, LocalDateTime.now())
                    .where(CANDIDATE_DOCUMENTS.CANDIDATE_ID.eq(candidateId))
                    .execute();
        } else {
            dsl.insertInto(CANDIDATE_DOCUMENTS)
                    .set(CANDIDATE_DOCUMENTS.CANDIDATE_ID, candidateId)
                    .set(CANDIDATE_DOCUMENTS.FILE_NAME, fileName)
                    .set(CANDIDATE_DOCUMENTS.FILE_PATH, filePath)
                    .set(CANDIDATE_DOCUMENTS.UPLOADED_BY, uploadedBy)
                    .execute();
        }
    }

    public List<CandidateResponse> findAll(Long jobId, String status, String search, int page, int size) {
        Condition condition = buildFilter(jobId, status, search);

        return dsl.select(CANDIDATES.asterisk(), USERS.FULL_NAME.as("added_by_name"), JOB_OPENINGS.TITLE.as("job_title"),
                        CANDIDATE_DOCUMENTS.FILE_NAME.as("resume_file_name"))
                .from(CANDIDATES)
                .leftJoin(USERS).on(CANDIDATES.ADDED_BY.eq(USERS.ID))
                .leftJoin(JOB_OPENINGS).on(CANDIDATES.JOB_ID.eq(JOB_OPENINGS.ID))
                .leftJoin(CANDIDATE_DOCUMENTS).on(CANDIDATES.ID.eq(CANDIDATE_DOCUMENTS.CANDIDATE_ID))
                .where(condition)
                .orderBy(CANDIDATES.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch(r -> {
                    CandidateResponse response = new CandidateResponse();
                    response.setId(r.get(CANDIDATES.ID));
                    response.setFullName(r.get(CANDIDATES.FULL_NAME));
                    response.setEmail(r.get(CANDIDATES.EMAIL));
                    response.setPhone(r.get(CANDIDATES.PHONE));
                    response.setExperienceYears(r.get(CANDIDATES.EXPERIENCE_YEARS));
                    response.setJobId(r.get(CANDIDATES.JOB_ID));
                    response.setJobTitle(r.get("job_title", String.class));
                    response.setStatus(CandidateStatus.valueOf(r.get(CANDIDATES.STATUS)));
                    response.setResumeFileName(r.get("resume_file_name", String.class));
                    response.setAddedById(r.get(CANDIDATES.ADDED_BY));
                    response.setAddedByName(r.get("added_by_name", String.class));
                    response.setCreatedAt(r.get(CANDIDATES.CREATED_AT));
                    response.setUpdatedAt(r.get(CANDIDATES.UPDATED_AT));
                    return response;
                });
    }
    public Long count(Long jobId, String status, String search) {
        return dsl.selectCount()
                .from(CANDIDATES)
                .where(buildFilter(jobId, status, search))
                .fetchOneInto(Long.class);
    }

    private Condition buildFilter(Long jobId, String status, String search) {
        Condition condition = DSL.noCondition();
        if (jobId != null) {
            condition = condition.and(CANDIDATES.JOB_ID.eq(jobId));
        }
        if (status != null && !status.isBlank()) {
            condition = condition.and(CANDIDATES.STATUS.eq(status));
        }
        if (search != null && !search.isBlank()) {
            condition = condition.and(
                    CANDIDATES.FULL_NAME.containsIgnoreCase(search)
                            .or(CANDIDATES.EMAIL.containsIgnoreCase(search))
            );
        }
        return condition;
    }

    public Optional<String> findFilePathByCandidateId(Long candidateId) {
        return Optional.ofNullable(
                dsl.select(CANDIDATE_DOCUMENTS.FILE_PATH)
                        .from(CANDIDATE_DOCUMENTS)
                        .where(CANDIDATE_DOCUMENTS.CANDIDATE_ID.eq(candidateId))
                        .fetchOne()
        ).map(record -> record.get(CANDIDATE_DOCUMENTS.FILE_PATH));
    }
}
