package com.example.url_shortner.dto;

public class OriginalUrlDto {
        private String url;

    public OriginalUrlDto(String url){
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
