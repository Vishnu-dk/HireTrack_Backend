package com.example.hiretrack.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound (ResourceNotFoundException ex){
        return build(HttpStatus.NOT_FOUND,ex.getMessage(),null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException ex){
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public  ResponseEntity<ApiError> conflict(ConflictException ex){
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validationFailed(MethodArgumentNotValidException ex){
        List<String> details=ex.getBindingResult().getFieldErrors().stream()
                .map(f-> f.getField()+" : "+f.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST,"Validation Error ",details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> exception(Exception ex){
        ex.printStackTrace();
        return build(HttpStatus.INTERNAL_SERVER_ERROR , "Unexpected server error", null);
    }

    private ResponseEntity<ApiError> build(HttpStatus httpStatus, String message, List<String > details) {
        return ResponseEntity.status(httpStatus)
                .body(new ApiError(httpStatus.value(),message, LocalDateTime.now(),details));
    }
}
