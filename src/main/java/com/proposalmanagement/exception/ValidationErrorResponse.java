package com.proposalmanagement.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse extends ErrorResponse {
    
    private Map<String, String> validationErrors;
    
    public ValidationErrorResponse() {}
    
    public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, 
                                 String message, String path, Map<String, String> validationErrors) {
        super(timestamp, status, error, message, path);
        this.validationErrors = validationErrors;
    }
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
    
    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}