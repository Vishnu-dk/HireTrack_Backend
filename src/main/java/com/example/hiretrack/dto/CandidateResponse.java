package com.example.hiretrack.dto;

import com.example.hiretrack.enums.CandidateStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private BigDecimal experienceYears;
    private Long jobId;
    private String jobTitle;
    private CandidateStatus status;
    private String resumeFileName;
    private Long addedById;
    private String addedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
