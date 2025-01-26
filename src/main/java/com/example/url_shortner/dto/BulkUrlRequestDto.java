package com.example.url_shortner.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BulkUrlRequestDto {

    private List<CreateUrlRequestDto> urls;
}
