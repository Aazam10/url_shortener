package com.example.url_shortner.exceptions;

import org.springframework.http.HttpStatus;

public class NotAuthorizedException extends RuntimeException {

    public HttpStatus httpStatus;
    public NotAuthorizedException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;

    }
}
