package com.example.url_shortner.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@Getter
@Setter
@NoArgsConstructor
public class NotAuthorizedException extends RuntimeException {

    private HttpStatus statusCode;
    public NotAuthorizedException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;

    }
}
