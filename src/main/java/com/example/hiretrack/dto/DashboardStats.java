package com.example.hiretrack.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private String role;
    private Integer totalJobs;
    private Integer openJobs;
    private Integer candidatesInPipeline;
    private Integer interviewsThisWeek;
    private Integer pendingFeedback;
}
