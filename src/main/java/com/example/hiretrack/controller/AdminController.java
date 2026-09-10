package com.example.hiretrack.controller;


import com.example.hiretrack.dto.AdminResponse;
import com.example.hiretrack.dto.PageResponse;
import com.example.hiretrack.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    @GetMapping("/recruiters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AdminResponse>> getAllRecruiters(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getAllRecruiters(status, search, page, size));
    }

    @GetMapping("/interviewers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AdminResponse>> getAllInterviewers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getAllInterviewers(status, search, page, size));
    }
}
