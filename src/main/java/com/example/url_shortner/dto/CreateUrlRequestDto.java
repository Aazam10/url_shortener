package com.example.url_shortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateUrlRequestDto {
    @NotBlank
    @Pattern(regexp = "^(http|https)://.*", message = "URL must start with http:// or https://")
    private String url;
    private LocalDateTime expiryDate;
    private String shortCode;
    private String password;
}
