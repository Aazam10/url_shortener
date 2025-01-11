package com.example.url_shortner.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException{

    public HttpStatus statusCode;
    public ResourceNotFoundException(String message, HttpStatus statusCode){
        super(message);
        this.statusCode = statusCode;
    }
}
