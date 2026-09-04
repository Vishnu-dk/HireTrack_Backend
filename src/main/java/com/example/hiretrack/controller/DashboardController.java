package com.example.hiretrack.controller;


import com.example.hiretrack.dto.DashboardStats;
import com.example.hiretrack.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;


    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INTERVIEWER','RECRUITER')")
    public ResponseEntity<DashboardStats> getDashboard(@AuthenticationPrincipal UserDetails user){
        DashboardStats response=dashboardService.getStatsForUser(user.getUsername());
        return ResponseEntity.ok(response);
    }
}
