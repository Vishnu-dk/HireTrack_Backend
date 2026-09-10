package com.example.hiretrack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RescheduleRequest {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // 👈 Add this line
    private LocalDateTime newScheduledAt;
    private int newDuration;
}
