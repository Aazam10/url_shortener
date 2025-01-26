package com.example.url_shortner.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ResourceNotFoundException extends RuntimeException{

    private HttpStatus statusCode;
    public ResourceNotFoundException(String message, HttpStatus statusCode){
        super(message);
        this.statusCode = statusCode;
    }
}
