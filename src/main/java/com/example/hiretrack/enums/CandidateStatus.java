package com.example.hiretrack.enums;

import lombok.Getter;

@Getter
public enum CandidateStatus {
    APPLIED(1),
    SHORTLISTED(2),
    INTERVIEW_SCHEDULED(3),
    SELECTED(4),
    REJECTED(4);

    private final int rank;

    CandidateStatus(int rank) {
        this.rank = rank;
    }

}

