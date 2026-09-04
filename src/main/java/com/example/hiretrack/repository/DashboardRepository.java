package com.example.hiretrack.repository;


import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public int countJobsByStatus(String status){
        return dsl.selectCount().from(JOB_OPENINGS)
                .where(JOB_OPENINGS.STATUS.eq(status))
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);
    }

    public int countCandidatesInPipeline(){
        return dsl.selectCount().from(CANDIDATES)
                .where(CANDIDATES.STATUS.in("APPLIED", "SHORTLISTED", "INTERVIEW_SCHEDULED"))
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);
    }

    public int countInterviewsThisWeek(){
        LocalDateTime startOfWeek= LocalDate.now().atStartOfDay();

        return dsl.selectCount().from(INTERVIEWS)
                .where(INTERVIEWS.SCHEDULED_AT.ge(startOfWeek))
                .and(INTERVIEWS.STATUS.ne("CANCELLED"))
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);
    }

    public int countPendingFeedback(){
        return dsl.selectCount().from(INTERVIEWS)
                .where(INTERVIEWS.STATUS.eq("COMPLETED"))
                .andNotExists(
                        DSL.selectOne().from(INTERVIEW_FEEDBACK)
                                .where(INTERVIEW_FEEDBACK.INTERVIEW_ID.eq(INTERVIEWS.ID))
                )
                .fetchOptional()
                .map(Record1::component1)
                .orElse(0);


    }
}
