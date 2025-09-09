package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.common.dto.CommonErrorDto;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

@ControllerAdvice
@Slf4j
public class CommonExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalArgumentException(IllegalArgumentException e) {
        log.error("[HANSOOM][ERROR] - CommonExceptionHandler/IllegalArgumentException - {}", e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFoundException(EntityNotFoundException e) {
        log.error("[HANSOOM][ERROR] - CommonExceptionHandler/EntityNotFoundException - {}", e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<?> entityExistsException(EntityExistsException e) {
        log.error("[HANSOOM][ERROR] - CommonExceptionHandler/EntityExistsException - {}", e.getMessage());
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.CONFLICT.value(), e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        log.error("[HANSOOM][ERROR] - CommonExceptionHandler/MethodArgumentNotValidException - {}", errorMessage);
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), errorMessage), HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(AuthorizationDeniedException.class)
//    public ResponseEntity<?> authorizationDeniedException(AuthorizationDeniedException e) {
//        log.error("[HANSOOM][ERROR] - CommonExceptionHandler/AuthorizationDeniedException - {}", e.getMessage());
//        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.FORBIDDEN.value(),  e.getMessage()), HttpStatus.FORBIDDEN);
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllException(Exception e) {
        log.error("[HANSOOM][ERROR] - CommonExceptionHandler/Exception - {}", e.getMessage());
        // MultipartException이 다른 예외로 감싸진 경우 체크
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof MultipartException) {
                log.error("[HANSOOM][ERROR] - Found wrapped MultipartException: {}", cause.getMessage());
                return handleMultipartException((MultipartException) cause, null);
            }
            cause = cause.getCause();
        }
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<CommonErrorDto> handleMultipartException(MultipartException ex, HttpServletRequest request) {
        log.error("=== MultipartException Detailed Analysis ===");
        log.error("[HANSOOM][ERROR] - Request URI: {}", request.getRequestURI());
        log.error("[HANSOOM][ERROR] - Content-Type: {}", request.getContentType());
        log.error("[HANSOOM][ERROR] - Content-Length: {}", request.getContentLength());
        log.error("[HANSOOM][ERROR] - MultipartException message: {}", ex.getMessage());

        // 원인(Cause) 체크
        Throwable cause = ex.getCause();
        int causeLevel = 0;
        while (cause != null && causeLevel < 5) {
            log.error("[HANSOOM][ERROR] - Cause level {}: {} - {}", causeLevel, cause.getClass().getSimpleName(), cause.getMessage());

            // 특정 원인들 체크
            if (cause instanceof java.io.IOException) {
                log.error("[HANSOOM][ERROR] - IOException detected - possibly file system or network issue");
            } else if (cause instanceof IllegalStateException) {
                log.error("[HANSOOM][ERROR] - IllegalStateException - possibly configuration issue");
            } else if (cause instanceof org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException) {
                log.error("[HANSOOM][ERROR] - File size limit exceeded!");
            } else if (cause instanceof org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException) {
                log.error("[HANSOOM][ERROR] - Request size limit exceeded!");
            }

            cause = cause.getCause();
            causeLevel++;
        }

        // 스택 트레이스도 출력
        log.error("[HANSOOM][ERROR] - Full stack trace: ", ex);

        return ResponseEntity.status(400)
                .body(new CommonErrorDto(400, "Multipart parsing failed: " + ex.getMessage()));
    }
}
