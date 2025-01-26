package com.example.url_shortner.exceptions;

import com.example.url_shortner.dto.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleNotFoundException(ResourceNotFoundException ex) {
        ApiErrorResponseDto  errorResponse = new ApiErrorResponseDto(ex.getStatusCode(),
                                                          "RESOURCE_NOT_FOUND",
                                                                    ex.getMessage());
        return new ResponseEntity<>(errorResponse,ex.getStatusCode());
    }

    @ExceptionHandler(EmptyUrlException.class)
    public ResponseEntity<ApiErrorResponseDto> handleEmptyUrlException(EmptyUrlException ex){
        ApiErrorResponseDto  errorResponse = new ApiErrorResponseDto(ex.getStatusCode(),
                "EMPTY_URL",
                ex.getMessage());
        return new ResponseEntity<>(errorResponse,ex.getStatusCode());

    }
    @ExceptionHandler(NoSuchUserFoundException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnkownUserException(NoSuchUserFoundException ex){
        ApiErrorResponseDto  errorResponse = new ApiErrorResponseDto(ex.getStatusCode(),
                "USER_NOT_FOUND",
                ex.getMessage());
        return new ResponseEntity<>(errorResponse,ex.getStatusCode());

    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnauthorizedException(NotAuthorizedException ex){
        ApiErrorResponseDto  errorResponse = new ApiErrorResponseDto(ex.getStatusCode(),
                "UNAUTHORIZED",
                ex.getMessage());
        return new ResponseEntity<>(errorResponse,ex.getStatusCode());

    }

    @ExceptionHandler(DuplicateShortCodeException.class)
    public ResponseEntity<ApiErrorResponseDto> handleExistingShortCode(DuplicateShortCodeException ex){
        ApiErrorResponseDto  errorResponse = new ApiErrorResponseDto(ex.getStatusCode(),
                "NOT_UNIQUE_SHORT_CODE",
                ex.getMessage());
        return new ResponseEntity<>(errorResponse,ex.getStatusCode());
    }
}
