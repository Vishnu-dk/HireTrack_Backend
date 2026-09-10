package com.example.hiretrack.dto;

import com.example.hiretrack.enums.InterviewStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewOverviewResponse {
    private Long interviewId;

    // Candidate (Student) Info
    private String candidateName;
    private String candidateEmail;

    // Recruiter Info
    private String recruiterName;
    private String recruiterEmail;

    //Interviewer Info
    private String interviewerName;
    private String interviewerEmail;

    // Interview Context
    private InterviewStatus status;
    private LocalDateTime scheduledAt;
    private int durationMinutes;
    private String Job;

    // Feedback Lifecycle Elements
    private boolean feedbackSubmitted;
    private String feedbackRecommendation;
}