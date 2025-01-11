package com.example.url_shortner.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class EmptyUrlException extends RuntimeException{

    public HttpStatus statusCode;
    public EmptyUrlException(String message, HttpStatus statusCode){
        super(message);
        this.statusCode = statusCode;
    }
}
