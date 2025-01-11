package com.example.url_shortner.dto;

public class ShortCodeResponseDto {



    private String short_code;

    public  ShortCodeResponseDto(String short_code){
        this.short_code = short_code;
    }

    public String getShort_code() {
        return short_code;
    }

    public void setShort_code(String short_code) {
        this.short_code = short_code;
    }
}
