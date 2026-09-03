package com.example.hiretrack.service;


import com.example.hiretrack.dto.FeedbackRequest;
import com.example.hiretrack.dto.FeedbackResponse;
import com.example.hiretrack.enums.InterviewStatus;
import com.example.hiretrack.exception.BadRequestException;
import com.example.hiretrack.exception.ResourceNotFoundException;
import com.example.hiretrack.jooq.tables.records.UsersRecord;
import com.example.hiretrack.repository.CandidateRepository;
import com.example.hiretrack.repository.FeedbackRepository;
import com.example.hiretrack.repository.InterviewRepository;
import com.example.hiretrack.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;


    public FeedbackService(FeedbackRepository feedbackRepository, InterviewRepository interviewRepository, CandidateRepository candidateRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.interviewRepository = interviewRepository;
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
    }

    public FeedbackResponse submitFeedback(Long interviewId, FeedbackRequest request,String interviewerEmail){
        if (feedbackRepository.existsByInterviewId(interviewId)){
            throw new BadRequestException("Feedback Already Submitted");
        }

        var interview = interviewRepository.findByIdWithDetails(interviewId)
                .orElseThrow(()->new ResourceNotFoundException("Interview Not Found"));

        if(!interview.getStatus().equals(InterviewStatus.COMPLETED)){
            throw new BadRequestException("Interview must be completed to submit record");
        }

        UsersRecord user=userRepository.findbyEmail(interviewerEmail)
                .orElseThrow(()->new ResourceNotFoundException("User not found"));

        if(!interview.getInterviewerId().equals(user.getId())){
            throw new BadRequestException("Only assigned interviewer can submit feedback");
        }

        feedbackRepository.create(interviewId, request.getTechnicalRating(), request.getCommunicationRating(),request.getRecommendation(),request.getComments(), user.getId());

        return feedbackRepository.findByInterviewId(interviewId).orElseThrow();

    }

    public FeedbackResponse getFeedback(Long interviewId){
        return feedbackRepository.findByInterviewId(interviewId)
                .orElseThrow(()->new ResourceNotFoundException("Interview not found"));
    }

    public List<FeedbackResponse> getCandidateFeedback(Long candidateId){
        if(candidateRepository.findById(candidateId).isEmpty()){
            throw new ResourceNotFoundException("Candidate not found");
        }
        List<FeedbackResponse> responses= feedbackRepository.findAllByCandidateId(candidateId);
        if(responses.isEmpty()){
            throw new ResourceNotFoundException("Candidate Feedback not generated");
        }
        return responses;
    }
}
