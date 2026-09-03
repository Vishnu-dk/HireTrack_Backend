package com.example.hiretrack.controller;


import com.example.hiretrack.dto.InterviewRequest;
import com.example.hiretrack.dto.InterviewResponse;
import com.example.hiretrack.jooq.tables.records.UsersRecord;
import com.example.hiretrack.security.JwtService;
import com.example.hiretrack.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;


    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<InterviewResponse> scheduleInterview(@Valid @RequestBody InterviewRequest request,
                                                               @AuthenticationPrincipal UserDetails user) {
        InterviewResponse response=interviewService.scheduleInterview(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterview(@PathVariable Long id){
        InterviewResponse response=interviewService.getInterview(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<List<InterviewResponse>> getMyInterviews(@AuthenticationPrincipal UserDetails user){
        List<InterviewResponse> responses= interviewService.getMyInterviewByEmail(user.getUsername());
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<InterviewResponse> cancelInterview(@PathVariable Long id ){
        InterviewResponse response=interviewService.cancelInterview(id);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('INTERVIEWER')")
    public  ResponseEntity<InterviewResponse> completeInterview (@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserDetails user){
        InterviewResponse response=interviewService.completeInterview(id, user.getUsername());
        return ResponseEntity.ok(response);
    }

}
