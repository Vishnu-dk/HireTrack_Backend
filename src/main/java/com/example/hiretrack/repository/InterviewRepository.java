package com.example.hiretrack.repository;


import com.example.hiretrack.dto.InterviewOverviewResponse;
import com.example.hiretrack.dto.InterviewResponse;
import com.example.hiretrack.enums.InterviewStatus;
import com.example.hiretrack.jooq.tables.Users;
import com.example.hiretrack.jooq.tables.records.InterviewsRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.hiretrack.jooq.tables.Candidates.CANDIDATES;
import static com.example.hiretrack.jooq.tables.InterviewFeedback.INTERVIEW_FEEDBACK;
import static com.example.hiretrack.jooq.tables.Interviews.INTERVIEWS;
import static com.example.hiretrack.jooq.tables.JobOpenings.JOB_OPENINGS;
import static com.example.hiretrack.jooq.tables.Users.USERS;

@Repository
public class InterviewRepository {

    private final DSLContext dsl;

    public InterviewRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean hasOverlap(Long interviewerId, Long excludeInterviewId, LocalDateTime newStart, LocalDateTime newEnd) {
        // Fetch only active interviews for this interviewer
        List<com.example.hiretrack.jooq.tables.records.InterviewsRecord> existing = dsl.selectFrom(INTERVIEWS)
                .where(INTERVIEWS.INTERVIEWER_ID.eq(interviewerId))
                .and(INTERVIEWS.STATUS.ne(InterviewStatus.CANCELLED.name()))
                .fetch();

        for (var rec : existing) {
            if (excludeInterviewId != null && rec.getId().equals(excludeInterviewId)) continue;

            LocalDateTime existingStart = rec.getScheduledAt();
            LocalDateTime existingEnd = existingStart.plusMinutes(rec.getDurationMinutes());

            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                return true;
            }
        }
        return false;
    }

    public Long hasActiveInterview(Long candidateId) {
        return dsl.selectCount()
                .from(INTERVIEWS)
                .where(INTERVIEWS.CANDIDATE_ID.eq(candidateId))
                .and(INTERVIEWS.STATUS.in(
                        InterviewStatus.SCHEDULED.name(),
                        InterviewStatus.RESCHEDULED.name()
                ))
                .fetchOne(0, Long.class);

    }


    public InterviewsRecord create(
            Long candidateId, Long jobId, Long interviewerId, LocalDateTime scheduledAt, int duration, Long createdBy) {
        return dsl.insertInto(INTERVIEWS)
                .set(INTERVIEWS.CANDIDATE_ID, candidateId)
                .set(INTERVIEWS.JOB_ID, jobId)
                .set(INTERVIEWS.INTERVIEWER_ID, interviewerId)
                .set(INTERVIEWS.SCHEDULED_AT, scheduledAt)
                .set(INTERVIEWS.DURATION_MINUTES, duration)
                .set(INTERVIEWS.STATUS, InterviewStatus.SCHEDULED.name())
                .set(INTERVIEWS.CREATED_BY, createdBy)
                .returning()
                .fetchOne();
    }

    public Optional<InterviewResponse> findByIdWithDetails(Long id) {
        Users users1 = USERS.as("USERS1");
        Users users2 = USERS.as("USERS2");

        return Optional.ofNullable(
                dsl.select(INTERVIEWS.asterisk(),
                                users1.FULL_NAME.as("interviewer_name"),
                                CANDIDATES.FULL_NAME.as("candidate_name"),
                                JOB_OPENINGS.TITLE.as("job_title"),
                                users2.FULL_NAME.as("created_by_name"))
                        .from(INTERVIEWS)
                        .leftJoin(users1).on(INTERVIEWS.INTERVIEWER_ID.eq(users1.ID))      // 👈 Changed to leftJoin
                        .leftJoin(CANDIDATES).on(INTERVIEWS.CANDIDATE_ID.eq(CANDIDATES.ID))  // 👈 Changed to leftJoin
                        .leftJoin(JOB_OPENINGS).on(INTERVIEWS.JOB_ID.eq(JOB_OPENINGS.ID))   // 👈 Changed to leftJoin
                        .leftJoin(users2).on(INTERVIEWS.CREATED_BY.eq(users2.ID))          // 👈 Changed to leftJoin
                        .where(INTERVIEWS.ID.eq(id))
                        .fetchOne(this::mapToResponse)
        );
    }


    public List<InterviewResponse> findByInterviewerId(Long interviewerId) {

        Users users1 = USERS.as("USERS1");
        Users users2 = USERS.as("USERS2");
        return dsl.select(INTERVIEWS.asterisk(),
                        users1.FULL_NAME.as("interviewer_name"),
                        CANDIDATES.FULL_NAME.as("candidate_name"),
                        JOB_OPENINGS.TITLE.as("job_title"),
                        users2.FULL_NAME.as("created_by_name"))
                .from(INTERVIEWS)
                .join(users1).on(INTERVIEWS.INTERVIEWER_ID.eq(users1.ID))
                .join(CANDIDATES).on(INTERVIEWS.CANDIDATE_ID.eq(CANDIDATES.ID))
                .join(JOB_OPENINGS).on(INTERVIEWS.JOB_ID.eq(JOB_OPENINGS.ID))
                .join(users2).on(INTERVIEWS.CREATED_BY.eq(users2.ID))
                .where(INTERVIEWS.INTERVIEWER_ID.eq(interviewerId))
                .and(INTERVIEWS.STATUS.ne(InterviewStatus.CANCELLED.name()))
                .orderBy(INTERVIEWS.SCHEDULED_AT.asc())
                .fetch(this::mapToResponse);
    }


    public void updateStatus(Long id,InterviewStatus status){
        dsl.update(INTERVIEWS)
                .set(INTERVIEWS.STATUS,status.name())
                .set(INTERVIEWS.UPDATED_AT,LocalDateTime.now())
                .where(INTERVIEWS.ID.eq(id))
                .execute();
    }


    public void updateSchedule(Long id,LocalDateTime newScheduleAt, int duration){
        int rowsUpdated = dsl.update(INTERVIEWS)
                .set(INTERVIEWS.SCHEDULED_AT, newScheduleAt)
                .set(INTERVIEWS.DURATION_MINUTES, duration)
                .set(INTERVIEWS.UPDATED_AT, LocalDateTime.now())
                .set(INTERVIEWS.STATUS,InterviewStatus.SCHEDULED.name())
                .where(INTERVIEWS.ID.eq(id))
                .execute();


    }

    private InterviewResponse mapToResponse(Record record) {
        if (record == null) return null;

        InterviewResponse response = new InterviewResponse();
        response.setId(record.get(INTERVIEWS.ID));
        response.setCandidateId(record.get(INTERVIEWS.CANDIDATE_ID));
        response.setCandidateName(record.get("candidate_name", String.class));
        response.setJobId(record.get(INTERVIEWS.JOB_ID));
        response.setJobTitle(record.get("job_title", String.class));
        response.setInterviewerId(record.get(INTERVIEWS.INTERVIEWER_ID));
        response.setInterviewerName(record.get("interviewer_name", String.class));
        response.setScheduledAt(record.get(INTERVIEWS.SCHEDULED_AT));
        response.setDurationMinutes(record.get(INTERVIEWS.DURATION_MINUTES));

        // 💡 Safe Enum Value Extraction
        String statusStr = record.get(INTERVIEWS.STATUS);
        if (statusStr != null) {
            try {
                response.setStatus(InterviewStatus.valueOf(statusStr.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("[DEBUG] Unknown interview status value: " + statusStr);
                response.setStatus(null);
            }
        } else {
            response.setStatus(null);
        }

        response.setCreatedById(record.get(INTERVIEWS.CREATED_BY));
        response.setCreatedByName(record.get("created_by_name", String.class));
        response.setCreatedAt(record.get(INTERVIEWS.CREATED_AT));
        response.setUpdatedAt(record.get(INTERVIEWS.UPDATED_AT));

        return response;
    }

    public List<InterviewOverviewResponse> findAllInterview(
            String status, String candidateName, String interviewerName, String jobTitle,
            int page, int size, Boolean isAdmin,Long userId) {

        Users interviewerTable = USERS.as("interviewerTable");
        Users recruiterTable = USERS.as("recruiterTable");

        Condition condition = buildAdminFilter(status, candidateName, interviewerName, jobTitle, interviewerTable,isAdmin,userId);

        return dsl.select(
                        INTERVIEWS.ID.as("interview_id"),
                        INTERVIEWS.STATUS.as("interview_status"),
                        INTERVIEWS.SCHEDULED_AT.as("scheduled_at"),
                        INTERVIEWS.DURATION_MINUTES.as("duration_minutes"),
                        CANDIDATES.FULL_NAME.as("candidate_name"),
                        CANDIDATES.EMAIL.as("candidate_email"),
                        recruiterTable.FULL_NAME.as("recruiter_name"),
                        recruiterTable.EMAIL.as("recruiter_email"),
                        interviewerTable.FULL_NAME.as("interviewer_name"),
                        interviewerTable.EMAIL.as("interviewer_email"),
                        JOB_OPENINGS.TITLE.as("job_title"),
                        INTERVIEW_FEEDBACK.RECOMMENDATION.as("recommendation")
                )
                .from(INTERVIEWS)
                .join(CANDIDATES).on(INTERVIEWS.CANDIDATE_ID.eq(CANDIDATES.ID))
                .join(JOB_OPENINGS).on(INTERVIEWS.JOB_ID.eq(JOB_OPENINGS.ID))
                .join(interviewerTable).on(INTERVIEWS.INTERVIEWER_ID.eq(interviewerTable.ID))
                .join(recruiterTable).on(INTERVIEWS.CREATED_BY.eq(recruiterTable.ID))
                .leftJoin(INTERVIEW_FEEDBACK).on(INTERVIEWS.ID.eq(INTERVIEW_FEEDBACK.INTERVIEW_ID))
                .where(condition)
                .orderBy(INTERVIEWS.SCHEDULED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch(record -> mapToOverviewResponse(record, isAdmin)); // 👈 Passed isAdmin here
    }

    public long countAdminOverview(String status, String candidateName, String interviewerName, String jobTitle,Boolean isAdmin,Long userId) {
        Users interviewerTable = USERS.as("interviewerTable");
        Users recruiterTable = USERS.as("recruiterTable");

        Condition condition = buildAdminFilter(status, candidateName, interviewerName, jobTitle, interviewerTable,isAdmin,userId);

        return dsl.selectCount()
                .from(INTERVIEWS)
                .join(CANDIDATES).on(INTERVIEWS.CANDIDATE_ID.eq(CANDIDATES.ID))
                .join(JOB_OPENINGS).on(INTERVIEWS.JOB_ID.eq(JOB_OPENINGS.ID))
                .join(interviewerTable).on(INTERVIEWS.INTERVIEWER_ID.eq(interviewerTable.ID))
                .join(recruiterTable).on(INTERVIEWS.CREATED_BY.eq(recruiterTable.ID))
                .leftJoin(INTERVIEW_FEEDBACK).on(INTERVIEWS.ID.eq(INTERVIEW_FEEDBACK.INTERVIEW_ID))
                .where(condition)
                .fetchOne(0, Long.class);
    }

    private Condition buildAdminFilter(String status, String candidateName, String interviewerName, String jobTitle, Users interviewerTable,Boolean isAdmin,Long userId) {
        Condition condition = DSL.noCondition();

        if (status != null && !status.trim().isEmpty()) {
            condition = condition.and(INTERVIEWS.STATUS.eq(status.toUpperCase()));
        }
        if (candidateName != null && !candidateName.trim().isEmpty()) {
            condition = condition.and(CANDIDATES.FULL_NAME.containsIgnoreCase(candidateName.trim()));
        }
        if (interviewerName != null && !interviewerName.trim().isEmpty()) {
            condition = condition.and(interviewerTable.FULL_NAME.containsIgnoreCase(interviewerName.trim()));
        }
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            condition = condition.and(JOB_OPENINGS.TITLE.containsIgnoreCase(jobTitle.trim()));
        }
        if(!isAdmin){
            condition=condition.and(
                    INTERVIEWS.CREATED_BY.eq(userId)
            );
        }

        return condition;
    }

    private InterviewOverviewResponse mapToOverviewResponse(Record record, boolean isAdmin) {
        InterviewOverviewResponse item = new InterviewOverviewResponse();
        item.setInterviewId(record.get("interview_id", Long.class));
        item.setStatus(InterviewStatus.valueOf(record.get("interview_status", String.class)));
        item.setScheduledAt(record.get("scheduled_at", LocalDateTime.class));
        item.setDurationMinutes(record.get("duration_minutes", Integer.class));

        item.setCandidateName(record.get("candidate_name", String.class));
        item.setCandidateEmail(record.get("candidate_email", String.class));

        if (isAdmin) {
            item.setRecruiterName(record.get("recruiter_name", String.class));
            item.setRecruiterEmail(record.get("recruiter_email", String.class));
        } else {
            item.setRecruiterName(null);
            item.setRecruiterEmail(null);
        }


        item.setInterviewerName(record.get("interviewer_name", String.class));
        item.setInterviewerEmail(record.get("interviewer_email", String.class));


        item.setJob(record.get("job_title", String.class));

        String recommendation = record.get("recommendation", String.class);
        item.setFeedbackSubmitted(recommendation != null);
        item.setFeedbackRecommendation(recommendation != null ? recommendation : "PENDING");

        return item;
    }


}
