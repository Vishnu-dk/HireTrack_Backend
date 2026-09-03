package com.example.hiretrack.service;

import com.example.hiretrack.dto.CandidateRequest;
import com.example.hiretrack.dto.CandidateResponse;
import com.example.hiretrack.dto.PageResponse;
import com.example.hiretrack.enums.CandidateStatus;
import com.example.hiretrack.exception.BadRequestException;
import com.example.hiretrack.exception.ResourceNotFoundException;
import com.example.hiretrack.jooq.tables.records.CandidatesRecord;
import com.example.hiretrack.jooq.tables.records.UsersRecord;
import com.example.hiretrack.repository.CandidateRepository;
import com.example.hiretrack.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CandidateService {
    private static final Set<String> VALID_STATUSES = Set.of("APPLIED", "SHORTLISTED", "INTERVIEW_SCHEDULED", "SELECTED", "REJECTED");
    private static final Set<CandidateStatus> FINAL_STATUSES = Set.of(CandidateStatus.SELECTED, CandidateStatus.REJECTED);

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public CandidateService(CandidateRepository candidateRepository, UserRepository userRepository) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;

    }

    @PostConstruct
    public void init() {
        createUploadDirectory();
    }

    private void createUploadDirectory() {
        if (uploadDir == null) {
            throw new IllegalStateException("The property app.upload.dir is missing or null!");
        }
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }


    public CandidateResponse create(CandidateRequest request,String userEmail){
        UsersRecord user=userRepository.findbyEmail(userEmail)
                .orElseThrow(()->new ResourceNotFoundException("User not Found"));

        CandidatesRecord record=candidateRepository.create(
                request.getFullName(),request.getEmail(),request.getPhone(),request.getExperienceYears(),request.getJobId(),user.getId()
        );
        return getCandidate(record.getId());

    }

    public CandidateResponse update(Long id,CandidateRequest request){
        CandidatesRecord candidate=getCandidateRecord(id);
        if(FINAL_STATUSES.contains(CandidateStatus.valueOf(candidate.getStatus()))){
            throw new BadRequestException("Cannot update a candidate who is already SELECTED or REJECTED");
        }
        candidateRepository.update(candidate.getId(),request.getFullName(),request.getEmail(), request.getPhone(), request.getExperienceYears());
        return getCandidate(id);
    }

    public CandidateResponse changeStatus(Long id,String newStatus){
        CandidatesRecord candidate=getCandidateRecord(id);
        CandidateStatus statusEnum;

        try{
            statusEnum=CandidateStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Allowed: APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED");
        }

        if (candidate.getStatus().equals(statusEnum.name())) {
            throw new BadRequestException("Candidate is already " + statusEnum);
        }
        if (FINAL_STATUSES.contains(CandidateStatus.valueOf(candidate.getStatus()))) {
            throw new BadRequestException("Cannot change status of a candidate who is already SELECTED or REJECTED");
        }
        candidateRepository.updateStatus(id, statusEnum);
        return getCandidate(id);

    }

    public PageResponse<CandidateResponse> getAllCandidates(Long jobId, String status, String search, int page, int size) {
        List<CandidateResponse> content = candidateRepository.findAll(jobId, status, search, page, size);
        long total = candidateRepository.count(jobId, status, search);
        int totalPages = (int) Math.ceil((double) total / size);

        PageResponse<CandidateResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(content);
        pageResponse.setPage(page);
        pageResponse.setSize(size);
        pageResponse.setTotalElements(total);
        pageResponse.setTotalPages(totalPages);
        return pageResponse;
    }


    public CandidateResponse uploadResume(Long id, MultipartFile file,String userEmail){
        if(file.isEmpty()){
            throw new BadRequestException("File is Empty");
        }

        candidateRepository.findFilePathByCandidateId(id)
                .ifPresent(oldFilePath -> {
                    File oldFile = new File(oldFilePath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                });

        String originalFilename=file.getOriginalFilename();
        String extension= originalFilename!=null&&originalFilename.contains(".")
                ?originalFilename.substring(originalFilename.lastIndexOf(".")):"pdf";
        String uniqueFileName= UUID.randomUUID().toString()+extension;
        Path filePath= Paths.get(uploadDir,uniqueFileName);

        try{
            Files.write(filePath,file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file "+ e);
        }

        UsersRecord user=userRepository.findbyEmail(userEmail)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        candidateRepository.addResume(id,originalFilename,filePath.toString(),user.getId());
        return getCandidate(id);
    }

    private CandidatesRecord getCandidateRecord(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));

    }

    public CandidateResponse getCandidate(Long id) {

        return candidateRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));

    }


}
