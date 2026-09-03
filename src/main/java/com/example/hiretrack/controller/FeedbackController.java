package com.example.hiretrack.controller;


import com.example.hiretrack.dto.FeedbackRequest;
import com.example.hiretrack.dto.FeedbackResponse;
import com.example.hiretrack.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews/{id}/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;


    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<FeedbackResponse> submitFeedback(@PathVariable Long id ,
                                                           @Valid @RequestBody FeedbackRequest request,
                                                           @AuthenticationPrincipal UserDetails user){
        FeedbackResponse response=feedbackService.submitFeedback(id,request,user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN') or hasRole('INTERVIEWER')")
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long id){
        FeedbackResponse response=feedbackService.getFeedback(id);
        return ResponseEntity.ok(response);
    }
}

