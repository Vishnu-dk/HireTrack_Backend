package com.example.hiretrack.dto;


import com.example.hiretrack.enums.Recommendation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long id;
    private Long interviewId;
    private String interviewerName;
    private int technicalRating;
    private int communicationRating;
    private Recommendation recommendation;
    private String comments;
    private LocalDateTime submittedAt;
}
