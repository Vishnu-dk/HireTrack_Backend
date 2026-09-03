package com.example.hiretrack.dto;

import com.example.hiretrack.enums.Recommendation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "Technical Rating is required")
    @Min(value = 1 ,message = "Rating must be 1 - 5")
    @Max(value = 5 ,message = "Rating must be 1 - 5")
    private Integer technicalRating;

    @NotNull(message = "Communication Rating is required")
    @Min(value = 1 ,message = "Rating must be 1 - 5")
    @Max(value = 5 ,message = "Rating must be 1 - 5")
    private Integer communicationRating;

    @NotNull(message = "Recommendation is required")
    private Recommendation recommendation;

    private String comments;

}
