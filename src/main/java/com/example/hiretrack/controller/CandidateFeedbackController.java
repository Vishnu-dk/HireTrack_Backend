package com.example.hiretrack.controller;

import com.example.hiretrack.dto.FeedbackResponse;
import com.example.hiretrack.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidates/{candidateId}/feedbacks")
public class CandidateFeedbackController {
    private final FeedbackService feedbackService;
    public CandidateFeedbackController(FeedbackService feedbackService) { this.feedbackService = feedbackService; }

    @GetMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<List<FeedbackResponse>> getAllFeedbacks(@PathVariable Long candidateId) {
        return ResponseEntity.ok(feedbackService.getCandidateFeedback(candidateId));
    }
}
