package com.example.hiretrack.service;

import com.example.hiretrack.dto.JobRequestDto;
import com.example.hiretrack.dto.JobResponseDto;
import com.example.hiretrack.dto.PageResponse;
import com.example.hiretrack.enums.JobStatus;
import com.example.hiretrack.exception.BadRequestException;
import com.example.hiretrack.exception.ResourceNotFoundException;
import com.example.hiretrack.jooq.tables.records.JobOpeningsRecord;
import com.example.hiretrack.jooq.tables.records.UsersRecord;
import com.example.hiretrack.mapper.JobOpeningRecordToJobResponseDto;
import com.example.hiretrack.repository.JobRepository;
import com.example.hiretrack.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class JobService {

    private static final Set<String> VALID_STATUSES = Set.of("OPEN", "ON_HOLD", "CLOSED");

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public JobResponseDto createJob(JobRequestDto request, String userEmail) {
        UsersRecord user = userRepository.findbyEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobOpeningsRecord record = jobRepository.create(
                request.getTitle(), request.getDescription(), request.getDepartment(), user.getId());

        return JobOpeningRecordToJobResponseDto.getJobResponseDto(record, user);
    }




    public JobResponseDto updateJob(Long id, JobRequestDto request) {
        JobOpeningsRecord job = getJobRecord(id);

        if (JobStatus.CLOSED.name().equals(job.getStatus())) {
            throw new BadRequestException("Cannot update a closed job opening");
        }

        jobRepository.update(id, request.getTitle(), request.getDescription(), request.getDepartment());
        return getJob(id);
    }

    public JobResponseDto changeStatus(Long id, String newStatus) {
        JobOpeningsRecord job = getJobRecord(id);

        JobStatus statusEnum;
        try {
            statusEnum = JobStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Allowed: OPEN, ON_HOLD, CLOSED");
        }


        if (job.getStatus().equals(statusEnum.name())) {
            throw new BadRequestException("Job is already " + statusEnum);
        }

        if (JobStatus.CLOSED.name().equals(job.getStatus())) {
            throw new BadRequestException("Cannot change status of a closed job");
        }

        jobRepository.updateStatus(id, statusEnum);
        return getJob(id);
    }

    public JobResponseDto getJob(Long id) {
        return jobRepository.findByIdWithCreator(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }

    public PageResponse<JobResponseDto> getAllJobs(String status, String search, int page, int size) {
        List<JobResponseDto> content = jobRepository.findAll(status, search, page, size);
        long total = jobRepository.count(status, search);
        int totalPages = (int) Math.ceil((double) total / size);

        PageResponse<JobResponseDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(content);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(total);
        pageResponse.setTotalPages(totalPages);

        return pageResponse;
    }

    private JobOpeningsRecord getJobRecord(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
    }
}