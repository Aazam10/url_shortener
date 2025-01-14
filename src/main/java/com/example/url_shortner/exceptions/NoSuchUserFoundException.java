package com.example.url_shortner.exceptions;

import org.springframework.http.HttpStatusCode;

public class NoSuchUserFoundException extends RuntimeException {

    public HttpStatusCode statusCode;

    public NoSuchUserFoundException(String message,HttpStatusCode statusCode){
        super(message);
        this.statusCode = statusCode;
    }
}
