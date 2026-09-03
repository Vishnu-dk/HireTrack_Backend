package com.example.hiretrack.controller;

import com.example.hiretrack.dto.JobRequestDto;
import com.example.hiretrack.dto.JobResponseDto;
import com.example.hiretrack.dto.PageResponse;
import com.example.hiretrack.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponseDto> createJob(@Valid @RequestBody JobRequestDto request,
                                                 @AuthenticationPrincipal UserDetails user) {
        JobResponseDto response=jobService.createJob(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponseDto> updateJob(@PathVariable Long id,
                                                 @Valid @RequestBody JobRequestDto request) {
        JobResponseDto response=jobService.updateJob(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<JobResponseDto> changeStatus(@PathVariable Long id,
                                                    @RequestParam String status) {
        JobResponseDto response=jobService.changeStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDto> getJob(@PathVariable Long id) {
        JobResponseDto response=jobService.getJob(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobResponseDto>> getAllJobs(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(jobService.getAllJobs(status, search, page, size));
    }
}