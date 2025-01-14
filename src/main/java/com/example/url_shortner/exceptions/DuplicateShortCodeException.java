package com.example.url_shortner.exceptions;

import org.springframework.http.HttpStatusCode;

public class DuplicateShortCodeException extends RuntimeException{

    public HttpStatusCode httpStatusCode;

    public DuplicateShortCodeException(String message,HttpStatusCode httpStatusCode){
        super(message);
        this.httpStatusCode = httpStatusCode;
    }
}
