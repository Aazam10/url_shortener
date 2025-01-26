package com.example.url_shortner.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
@NoArgsConstructor
public class NoSuchUserFoundException extends RuntimeException {

    private HttpStatus statusCode;

    public NoSuchUserFoundException(String message,HttpStatus statusCode){
        super(message);
        this.statusCode = statusCode;
    }
}
