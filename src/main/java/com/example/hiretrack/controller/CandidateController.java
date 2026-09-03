package com.example.hiretrack.controller;


import com.example.hiretrack.dto.CandidateRequest;
import com.example.hiretrack.dto.CandidateResponse;
import com.example.hiretrack.dto.PageResponse;
import com.example.hiretrack.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<CandidateResponse> createCandidate(@Valid @RequestBody CandidateRequest request,
                                                             @AuthenticationPrincipal UserDetails user){

        CandidateResponse response=candidateService.create(request,user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<CandidateResponse> updateCandidate(@PathVariable Long id,
                                                             @Valid @RequestBody CandidateRequest request) {
        CandidateResponse response=candidateService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<CandidateResponse> changeStatus(@PathVariable Long id,
                                                          @RequestParam String status) {

        CandidateResponse response=candidateService.changeStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> getCandidate(@PathVariable Long id) {

        CandidateResponse response=candidateService.getCandidate(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<CandidateResponse>> getAllCandidates(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.ok(candidateService.getAllCandidates(jobId,status,search,page,size));
    }

    @PostMapping(value = "/{id}/resume" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<CandidateResponse> uploadResume(@PathVariable Long id,
                                                          @RequestParam("file")MultipartFile file,
                                                          @AuthenticationPrincipal UserDetails user){
        CandidateResponse response=candidateService.uploadResume(id,file, user.getUsername());
        return ResponseEntity.ok(response);
    }
}
