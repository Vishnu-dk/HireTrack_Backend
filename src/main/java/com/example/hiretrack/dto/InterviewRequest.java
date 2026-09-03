package com.example.hiretrack.dto;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRequest {

    @NotNull(message = "Candidate Id required")
    private Long candidateId;

    @NotNull(message = "Interviewer Id required")
    private Long interviewerId;

    @NotNull(message = "Interview date required")
    @Future(message = "Interview Date must be in future")
    private LocalDateTime scheduledAt;

    private int durationMinutes=45;

}
