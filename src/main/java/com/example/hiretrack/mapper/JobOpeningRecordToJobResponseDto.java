package com.example.hiretrack.mapper;

import com.example.hiretrack.dto.JobResponseDto;
import com.example.hiretrack.enums.JobStatus;
import com.example.hiretrack.jooq.tables.records.JobOpeningsRecord;
import com.example.hiretrack.jooq.tables.records.UsersRecord;

public class JobOpeningRecordToJobResponseDto {
    public static JobResponseDto getJobResponseDto(JobOpeningsRecord record, UsersRecord user) {
        JobResponseDto response = new JobResponseDto();
        response.setId(record.getId());
        response.setTitle(record.getTitle());
        response.setDescription(record.getDescription());
        response.setDepartment(record.getDepartment());
        response.setStatus(JobStatus.valueOf(record.getStatus()));
        response.setCreatedById(record.getCreatedBy());
        response.setCreatedByName(user.getFullName());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
}
