package com.example.url_shortner.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponseDto {

        private HttpStatus status;
        private String errorCode;
        private String message;

}


