package com.revature.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    public GlobalExceptionHandler() {
        System.out.println("GLOBAL EXCEPTION HANDLER INITIALIZED");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        System.out.println("CATCHING MethodArgumentNotValidException: " + ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        errors.put("error", errorMessage);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationExceptions(org.springframework.security.core.AuthenticationException ex) {
        System.out.println("CATCHING AuthenticationException: " + ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Invalid username or password");
        return new ResponseEntity<>(errors, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsExceptions(org.springframework.security.authentication.BadCredentialsException ex) {
        System.out.println("CATCHING BadCredentialsException: " + ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Invalid username or password");
        return new ResponseEntity<>(errors, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Internal Server Error: " + ex.getMessage());
        errors.put("type", ex.getClass().getName());
        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
