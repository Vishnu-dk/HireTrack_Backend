package com.example.hiretrack.controller;


import com.example.hiretrack.dto.*;
import com.example.hiretrack.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<InterviewResponse> rescheduleInterview(@PathVariable Long id ,
                                                                 @Valid @RequestBody RescheduleRequest request,
                                                                 @AuthenticationPrincipal UserDetails user){
        InterviewResponse response=interviewService.rescheduleInterview(id,request.getNewScheduledAt(),request.getNewDuration());
        return ResponseEntity.ok(response);
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
    @GetMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<PageResponse<InterviewOverviewResponse>> getAllInterviews(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String candidateName,
            @RequestParam(required = false) String interviewerName,
            @RequestParam(required = false) String job,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails user,
            Authentication authentication
) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ADMIN"));


        return ResponseEntity.ok(interviewService.getAllInterviews(status, candidateName, interviewerName,job, page, size,isAdmin,user.getUsername()));
    }

}
