package com.example.hiretrack.repository;


import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.example.hiretrack.jooq.tables.Candidates.CANDIDATES;
import static com.example.hiretrack.jooq.tables.InterviewFeedback.INTERVIEW_FEEDBACK;
import static com.example.hiretrack.jooq.tables.Interviews.INTERVIEWS;
import static com.example.hiretrack.jooq.tables.JobOpenings.JOB_OPENINGS;

@Repository
public class DashboardRepository {

    private final DSLContext dsl;

    public DashboardRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public int countJobsByStatus(String status,String role,Long userId){
        Condition condition = DSL.noCondition();

        if (!"ADMIN".equalsIgnoreCase(role)) {
            condition = condition.and(JOB_OPENINGS.CREATED_BY.eq(userId));
        }

        if (status == null) {
            condition = condition.and(JOB_OPENINGS.STATUS.in("OPEN", "ON_HOLD", "CLOSED"));
        } else {
            condition = condition.and(JOB_OPENINGS.STATUS.eq(status));
        }

        return dsl.selectCount()
                .from(JOB_OPENINGS)
                .where(condition)
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);
    }

    public int countCandidatesInPipeline(String role, Long userId) {
        Condition condition = CANDIDATES.STATUS.in("APPLIED", "SHORTLISTED", "INTERVIEW_SCHEDULED");

        if (!"ADMIN".equalsIgnoreCase(role)) {
            condition = condition.and(JOB_OPENINGS.CREATED_BY.eq(userId));
        }

        return dsl.selectCount()
                .from(CANDIDATES)
                .leftJoin(JOB_OPENINGS).on(CANDIDATES.JOB_ID.eq(JOB_OPENINGS.ID))
                .where(condition)
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);
    }

    public int countInterviewsThisWeek(String role, Long userId) {
        LocalDateTime startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        Condition condition = INTERVIEWS.SCHEDULED_AT.ge(startOfWeek)
                .and(INTERVIEWS.SCHEDULED_AT.lt(endOfWeek))
                .and(INTERVIEWS.STATUS.ne("CANCELLED"));

        if ("INTERVIEWER".equalsIgnoreCase(role)) {
            condition = condition.and(INTERVIEWS.INTERVIEWER_ID.eq(userId));
        } else if (!"ADMIN".equalsIgnoreCase(role)) {
            condition = condition.and(JOB_OPENINGS.CREATED_BY.eq(userId));
        }

        return dsl.selectCount()
                .from(INTERVIEWS)
                .leftJoin(JOB_OPENINGS).on(INTERVIEWS.JOB_ID.eq(JOB_OPENINGS.ID))
                .where(condition)
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);
    }

    public int countPendingFeedback(String role, Long userId) {
        Condition condition = INTERVIEWS.STATUS.eq("COMPLETED")
                .andNotExists(
                        DSL.selectOne().from(INTERVIEW_FEEDBACK)
                                .where(INTERVIEW_FEEDBACK.INTERVIEW_ID.eq(INTERVIEWS.ID))
                );

        if ("INTERVIEWER".equalsIgnoreCase(role)) {
            condition = condition.and(INTERVIEWS.INTERVIEWER_ID.eq(userId));
        } else if (!"ADMIN".equalsIgnoreCase(role)) {
            condition = condition.and(JOB_OPENINGS.CREATED_BY.eq(userId));
        }

        return dsl.selectCount()
                .from(INTERVIEWS)
                .leftJoin(JOB_OPENINGS).on(INTERVIEWS.JOB_ID.eq(JOB_OPENINGS.ID))
                .where(condition)
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);
    }
}
