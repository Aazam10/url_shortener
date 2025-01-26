package com.example.url_shortner.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@NoArgsConstructor
@Setter
public class BulkShortCodeResponseDto {

    List<UrlDetailsResponseDto> shortCodes;
}
