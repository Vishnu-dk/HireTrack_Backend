package com.example.hiretrack.service;

import com.example.hiretrack.dto.AdminResponse;
import com.example.hiretrack.dto.PageResponse;
import com.example.hiretrack.jooq.tables.records.UsersRecord;
import com.example.hiretrack.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AdminService {


    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public PageResponse<AdminResponse> getAllRecruiters(String status, String search, int page, int size) {
        return fetchUsersPageResponse("RECRUITER", status, search, page, size);
    }

    public PageResponse<AdminResponse> getAllInterviewers(String status, String search, int page, int size) {
        return fetchUsersPageResponse("INTERVIEWER", status, search, page, size);
    }

    private PageResponse<AdminResponse> fetchUsersPageResponse(String role, String status, String search, int page, int size) {
        List<UsersRecord> records = adminRepository.getAllUsersByRole(role, status, search, page, size);
        long totalElements = adminRepository.getUsersCountByRole(role, status, search);

        List<AdminResponse> content = records.stream().map(record -> {
            AdminResponse dto = new AdminResponse();
            dto.setId(record.getId());
            dto.setFullName(record.getFullName());
            dto.setEmail(record.getEmail());
            dto.setIsActive(record.getActive());
            dto.setCreatedAt(record.getCreatedAt());
            dto.setUpdatedAt(record.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / size);

        PageResponse<AdminResponse> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);

        return response;
    }
}
