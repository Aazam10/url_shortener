package com.example.url_shortner.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShortCodeResponseDto {
    private String shortCode;

    private String originalUrl;

    private LocalDateTime expiryDate;

}
