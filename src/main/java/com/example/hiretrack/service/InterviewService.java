package com.example.hiretrack.service;


import com.example.hiretrack.dto.CandidateRequest;
import com.example.hiretrack.dto.InterviewRequest;
import com.example.hiretrack.dto.InterviewResponse;
import com.example.hiretrack.enums.CandidateStatus;
import com.example.hiretrack.enums.InterviewStatus;
import com.example.hiretrack.exception.BadRequestException;
import com.example.hiretrack.exception.ResourceNotFoundException;
import com.example.hiretrack.jooq.tables.records.CandidatesRecord;
import com.example.hiretrack.jooq.tables.records.InterviewsRecord;
import com.example.hiretrack.jooq.tables.records.UsersRecord;
import com.example.hiretrack.repository.CandidateRepository;
import com.example.hiretrack.repository.InterviewRepository;
import com.example.hiretrack.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;


    public InterviewService(InterviewRepository interviewRepository, CandidateRepository candidateRepository, UserRepository userRepository) {
        this.interviewRepository = interviewRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
    }

    public InterviewResponse scheduleInterview(InterviewRequest request, String recruiterEmail) {
        CandidatesRecord candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));

        if (candidate.getStatus().equals(CandidateStatus.REJECTED.name()) || candidate.getStatus().equals(CandidateStatus.SELECTED.name())) {
            throw new BadRequestException("Cannot Schedule interview for SELECTED or REJECTED candidates");
        }
        if (interviewRepository.hasActiveInterview(request.getCandidateId())>0) {
            throw new BadRequestException("Candidate already has an active interview scheduled");
        }
        UsersRecord interviewer = userRepository.findById(request.getInterviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer not found "));

        if (!"INTERVIEWER".equals(interviewer.getRole())) {
            throw new BadRequestException("User is not an interviewer");
        }
        LocalDateTime newEnd = request.getScheduledAt().plusMinutes(request.getDurationMinutes());

        if (interviewRepository.hasOverlap(request.getInterviewerId(), null, request.getScheduledAt(), newEnd)) {
            throw new BadRequestException("Interview is occupied in this time slot");
        }

        UsersRecord recruiter = userRepository.findbyEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        InterviewsRecord interview = interviewRepository.create(
                request.getCandidateId(), candidate.getJobId(), request.getInterviewerId(),
                request.getScheduledAt(), request.getDurationMinutes(), recruiter.getId());

        candidateRepository.updateStatus(candidate.getId(), CandidateStatus.INTERVIEW_SCHEDULED);

        return interviewRepository.findByIdWithDetails(interview.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Failed to retrieve create interview"));
    }

    public InterviewResponse getInterview(Long id){
        return interviewRepository.findByIdWithDetails(id)
                .orElseThrow(()-> new ResourceNotFoundException("Interview not found"));
    }

    public InterviewResponse rescheduleInterview (Long id , LocalDateTime newScheduledAt, int newDuration){
        LocalDateTime newEnd=newScheduledAt.plusMinutes(newDuration);

        if(interviewRepository.hasOverlap(null,id,newScheduledAt,newEnd)){
            throw new BadRequestException("This slot is already booked");
        }

        interviewRepository.updateSchedule(id,newScheduledAt,newDuration);
        return getInterview(id);
    }

    public InterviewResponse cancelInterview(Long id) {
        var interview = interviewRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        InterviewStatus status = interview.getStatus();
        if (status != InterviewStatus.SCHEDULED && status != InterviewStatus.RESCHEDULED) {
            if (status == InterviewStatus.COMPLETED) {
                throw new BadRequestException("Cannot complete an interview that has been completed.");
            } else if (status==InterviewStatus.CANCELLED) {
                throw new BadRequestException("Interview Already cancelled");
            }
            throw new BadRequestException("Interview must be in SCHEDULED or RESCHEDULED status to be cancelled.");
        }

        interviewRepository.updateStatus(id, InterviewStatus.CANCELLED);

        return interviewRepository.findByIdWithDetails(id).orElseThrow();
    }

    public InterviewResponse completeInterview(Long id, String interviewerEmail) {
        UsersRecord interviewer = userRepository.findbyEmail(interviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var interview = interviewRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        if (!interview.getInterviewerId().equals(interviewer.getId())) {
            throw new BadRequestException("Only the assigned interviewer can mark this as complete");
        }

        InterviewStatus status = interview.getStatus();
        if (status != InterviewStatus.SCHEDULED && status != InterviewStatus.RESCHEDULED) {
            if (status == InterviewStatus.CANCELLED) {
                throw new BadRequestException("Cannot complete an interview that has been cancelled.");
            }
            throw new BadRequestException("Interview must be in SCHEDULED or RESCHEDULED status to be completed.");
        }


        interviewRepository.updateStatus(id, InterviewStatus.COMPLETED);
        return interviewRepository.findByIdWithDetails(id).orElseThrow();
    }

    public List<InterviewResponse> getMyInterviewByEmail(String interviewerEmail) {
        UsersRecord user=userRepository.findbyEmail(interviewerEmail)
                .orElseThrow(()->new ResourceNotFoundException("User Not Found"));

        return interviewRepository.findByInterviewerId(user.getId());


    }
}
