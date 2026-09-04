package com.example.hiretrack.service;


import com.example.hiretrack.dto.DashboardStats;
import com.example.hiretrack.exception.ResourceNotFoundException;
import com.example.hiretrack.repository.DashboardRepository;
import com.example.hiretrack.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final UserRepository userRepository;

    public DashboardService(DashboardRepository dashboardRepository, UserRepository userRepository) {
        this.dashboardRepository = dashboardRepository;
        this.userRepository = userRepository;
    }

    public DashboardStats getStatsForUser(String email){

        var user= userRepository.findbyEmail(email)
                .orElseThrow(()-> new ResourceNotFoundException("User Not Found"));

        String role=user.getRole();

        DashboardStats stats= new DashboardStats();
        stats.setRole(role);

        if ("ADMIN".equals(role) || "RECRUITER".equals(role)) {
            stats.setTotalJobs(dashboardRepository.countJobsByStatus(null));
            stats.setOpenJobs(dashboardRepository.countJobsByStatus("OPEN"));
            stats.setCandidatesInPipeline(dashboardRepository.countCandidatesInPipeline());
            stats.setInterviewsThisWeek(dashboardRepository.countInterviewsThisWeek());
            stats.setPendingFeedback(dashboardRepository.countPendingFeedback());
        } else if ("INTERVIEWER".equals(role)) {
            stats.setInterviewsThisWeek(dashboardRepository.countInterviewsThisWeek());
            stats.setPendingFeedback(dashboardRepository.countPendingFeedback());
        }

        return stats;
    }
}
