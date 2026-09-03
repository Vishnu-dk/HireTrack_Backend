package com.example.hiretrack.dto;


import com.example.hiretrack.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {

    private long id;
    private String title;
    private String description;
    private String department;
    private JobStatus Status;
    private long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
