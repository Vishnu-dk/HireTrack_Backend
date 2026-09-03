package com.example.hiretrack.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        int status,
        String message,
        LocalDateTime timeStamp,
        List<String> details
) {
}
