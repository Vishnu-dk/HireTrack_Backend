package com.example.hiretrack.repository;


import com.example.hiretrack.dto.FeedbackResponse;
import com.example.hiretrack.enums.Recommendation;
import com.example.hiretrack.jooq.tables.records.InterviewFeedbackRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.hiretrack.jooq.tables.InterviewFeedback.INTERVIEW_FEEDBACK;
import static com.example.hiretrack.jooq.tables.Interviews.INTERVIEWS;
import static com.example.hiretrack.jooq.tables.Users.USERS;

@Repository
public class FeedbackRepository {
    private final DSLContext dsl;

    public FeedbackRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean existsByInterviewId(Long interviewId) {
        return dsl.fetchExists(
                dsl.selectOne()
                        .from(INTERVIEW_FEEDBACK)
                        .where(INTERVIEW_FEEDBACK.INTERVIEW_ID.eq(interviewId))
        );
    }


    public InterviewFeedbackRecord create(
            Long interviewId, int technicalRating, int communicationRating, Recommendation recommendation, String comments, Long submittedBy) {
        return dsl.insertInto(INTERVIEW_FEEDBACK)
                .set(INTERVIEW_FEEDBACK.INTERVIEW_ID,interviewId)
                .set(INTERVIEW_FEEDBACK.TECHNICAL_RATING,technicalRating)
                .set(INTERVIEW_FEEDBACK.COMMUNICATION_RATING,communicationRating)
                .set(INTERVIEW_FEEDBACK.RECOMMENDATION,recommendation.name())
                .set(INTERVIEW_FEEDBACK.COMMENTS,comments)
                .set(INTERVIEW_FEEDBACK.SUBMITTED_AT, LocalDateTime.now())
                .set(INTERVIEW_FEEDBACK.SUBMITTED_BY,submittedBy)
                .returning()
                .fetchOne();
    }

    public Optional<FeedbackResponse> findByInterviewId(Long interviewId){
        return Optional.ofNullable(
                dsl.select(INTERVIEW_FEEDBACK.asterisk(),USERS.FULL_NAME)
                        .from(INTERVIEW_FEEDBACK)
                        .join(USERS).on(INTERVIEW_FEEDBACK.SUBMITTED_BY.eq(USERS.ID))
                        .where(INTERVIEW_FEEDBACK.INTERVIEW_ID.eq(interviewId))
                        .fetchOne(this::mapResponse)
        );
    }

    public List<FeedbackResponse> findAllByCandidateId(Long candidateId){
        return dsl.select(INTERVIEW_FEEDBACK.asterisk(),USERS.FULL_NAME)
                .from(INTERVIEW_FEEDBACK)
                .join(INTERVIEWS).on(INTERVIEW_FEEDBACK.INTERVIEW_ID.eq(INTERVIEWS.ID))
                .join(USERS).on(INTERVIEW_FEEDBACK.SUBMITTED_BY.eq(USERS.ID))
                .where(INTERVIEWS.CANDIDATE_ID.eq(candidateId))
                .fetch(this::mapResponse);
    }

    private FeedbackResponse mapResponse(Record record) {
        FeedbackResponse response = new FeedbackResponse();
        response.setId(record.get(INTERVIEW_FEEDBACK.ID));
        response.setInterviewId(record.get(INTERVIEW_FEEDBACK.INTERVIEW_ID));
        response.setInterviewerName(record.get(USERS.FULL_NAME));
        response.setTechnicalRating(record.get(INTERVIEW_FEEDBACK.TECHNICAL_RATING));
        response.setCommunicationRating(record.get(INTERVIEW_FEEDBACK.COMMUNICATION_RATING));
        response.setRecommendation(Recommendation.valueOf(record.get(INTERVIEW_FEEDBACK.RECOMMENDATION)));
        response.setComments(record.get(INTERVIEW_FEEDBACK.COMMENTS));
        response.setSubmittedAt(record.get(INTERVIEW_FEEDBACK.SUBMITTED_AT));
        return response;
    }
}
