package com.example.hiretrack.repository;

import com.example.hiretrack.dto.JobResponseDto;
import com.example.hiretrack.enums.JobStatus;
import com.example.hiretrack.jooq.tables.records.JobOpeningsRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.hiretrack.jooq.tables.JobOpenings.JOB_OPENINGS;
import static com.example.hiretrack.jooq.tables.Users.USERS;

@Repository
public class JobRepository {

    private final DSLContext dsl;

    public JobRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public JobOpeningsRecord create(String title, String description, String department, Long createdBy) {
        return dsl.insertInto(JOB_OPENINGS)
                .set(JOB_OPENINGS.TITLE, title)
                .set(JOB_OPENINGS.DESCRIPTION, description)
                .set(JOB_OPENINGS.DEPARTMENT, department)
                .set(JOB_OPENINGS.CREATED_BY, createdBy)
                .returning()
                .fetchOne();
    }

    public Optional<JobOpeningsRecord> findById(Long id) {
        return Optional.ofNullable(
                dsl.selectFrom(JOB_OPENINGS).where(JOB_OPENINGS.ID.eq(id)).fetchOne()
        );
    }

    public Optional<JobResponseDto> findByIdWithCreator(Long id) {
        return Optional.ofNullable(
                dsl.select(JOB_OPENINGS.asterisk(), USERS.FULL_NAME)
                        .from(JOB_OPENINGS)
                        .leftJoin(USERS).on(JOB_OPENINGS.CREATED_BY.eq(USERS.ID))
                        .where(JOB_OPENINGS.ID.eq(id))
                        .fetchOne(r -> {
                            JobResponseDto response = new JobResponseDto();
                            response.setId(r.get(JOB_OPENINGS.ID));
                            response.setTitle(r.get(JOB_OPENINGS.TITLE));
                            response.setDescription(r.get(JOB_OPENINGS.DESCRIPTION));
                            response.setDepartment(r.get(JOB_OPENINGS.DEPARTMENT));
                            response.setStatus(JobStatus.valueOf(r.get(JOB_OPENINGS.STATUS)));
                            response.setCreatedById(r.get(JOB_OPENINGS.CREATED_BY));
                            response.setCreatedByName(r.get(USERS.FULL_NAME));
                            response.setCreatedAt(r.get(JOB_OPENINGS.CREATED_AT));
                            response.setUpdatedAt(r.get(JOB_OPENINGS.UPDATED_AT));
                            return response;
                        })
        );
    }

    public void update(Long id, String title, String description, String department) {
        dsl.update(JOB_OPENINGS)
                .set(JOB_OPENINGS.TITLE, title)
                .set(JOB_OPENINGS.DESCRIPTION, description)
                .set(JOB_OPENINGS.DEPARTMENT, department)
                .set(JOB_OPENINGS.UPDATED_AT, LocalDateTime.now())
                .where(JOB_OPENINGS.ID.eq(id))
                .execute();
    }

    public void updateStatus(Long id, JobStatus status) {
        dsl.update(JOB_OPENINGS)
                .set(JOB_OPENINGS.STATUS, status.name())
                .set(JOB_OPENINGS.UPDATED_AT, LocalDateTime.now())
                .where(JOB_OPENINGS.ID.eq(id))
                .execute();
    }

    public List<JobResponseDto> findAll(String status, String search, int page, int size) {
        Condition condition = buildFilter(status, search);

        return dsl.select(JOB_OPENINGS.asterisk(), USERS.FULL_NAME)
                .from(JOB_OPENINGS)
                .leftJoin(USERS).on(JOB_OPENINGS.CREATED_BY.eq(USERS.ID))
                .where(condition)
                .orderBy(JOB_OPENINGS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch(r -> {
                    JobResponseDto response = new JobResponseDto();
                    response.setId(r.get(JOB_OPENINGS.ID));
                    response.setTitle(r.get(JOB_OPENINGS.TITLE));
                    response.setDescription(r.get(JOB_OPENINGS.DESCRIPTION));
                    response.setDepartment(r.get(JOB_OPENINGS.DEPARTMENT));
                    response.setStatus(JobStatus.valueOf(r.get(JOB_OPENINGS.STATUS)));
                    response.setCreatedById(r.get(JOB_OPENINGS.CREATED_BY));
                    response.setCreatedByName(r.get(USERS.FULL_NAME));
                    response.setCreatedAt(r.get(JOB_OPENINGS.CREATED_AT));
                    response.setUpdatedAt(r.get(JOB_OPENINGS.UPDATED_AT));
                    return response;
                });
    }

    public long count(String status, String search) {
        return dsl.selectCount()
                .from(JOB_OPENINGS)
                .where(buildFilter(status, search))
                .fetchOne(0, long.class);
    }

    private Condition buildFilter(String status, String search) {
        Condition condition = DSL.noCondition();

        if (status != null && !status.isBlank()) {
            condition = condition.and(JOB_OPENINGS.STATUS.eq(status));
        }
        if (search != null && !search.isBlank()) {
            condition = condition.and(
                    JOB_OPENINGS.TITLE.containsIgnoreCase(search)
                            .or(JOB_OPENINGS.DEPARTMENT.containsIgnoreCase(search))
            );
        }
        return condition;
    }
}