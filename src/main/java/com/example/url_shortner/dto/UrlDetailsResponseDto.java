package com.example.url_shortner.dto;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlDetailsResponseDto {
    private String originalUrl;
    private String shortCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiryDate;
    private Integer numVisited;
    private LocalDateTime lastVisitedAt;
 }
