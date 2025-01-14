package com.example.url_shortner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class InactiveUrlRequestDto {

    private LocalDateTime expiryDate;
}
