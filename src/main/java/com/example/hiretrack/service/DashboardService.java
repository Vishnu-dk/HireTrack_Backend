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
            stats.setTotalJobs(dashboardRepository.countJobsByStatus(null,user.getRole(),user.getId()));
            stats.setOpenJobs(dashboardRepository.countJobsByStatus("OPEN",user.getRole(),user.getId()));
            stats.setCandidatesInPipeline(dashboardRepository.countCandidatesInPipeline(user.getRole(),user.getId()));
            stats.setInterviewsThisWeek(dashboardRepository.countInterviewsThisWeek(user.getRole(),user.getId()));
            stats.setPendingFeedback(dashboardRepository.countPendingFeedback(user.getRole(),user.getId()));
        } else if ("INTERVIEWER".equals(role)) {
            stats.setInterviewsThisWeek(dashboardRepository.countInterviewsThisWeek(user.getRole(),user.getId()));
            stats.setPendingFeedback(dashboardRepository.countPendingFeedback(user.getRole(),user.getId()));
        }

        return stats;
    }
}
