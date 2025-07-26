package com.beyond.HanSoom.common;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.MappedSuperclass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
        return new ResponseEntity<>(new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
