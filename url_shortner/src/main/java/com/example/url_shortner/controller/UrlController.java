package com.example.url_shortner.controller;

import com.example.url_shortner.dto.OriginalUrlDto;
import com.example.url_shortner.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;


@RestController

public class UrlController {

    private final UrlService urlService;

    public  UrlController(UrlService urlService){
        this.urlService = urlService;
    }
    @PostMapping(path = "/shorten")
    public ResponseEntity<?> createShortUrl(@RequestBody OriginalUrlDto originalUrlDto){
        String url = originalUrlDto.getUrl();

        return urlService.createShortUrl(url);
    }

    @GetMapping("/redirect")
    public RedirectView redirectShortUrl(@RequestParam("code") String code){

        try {
            String originalUrl = urlService.getOriginalUrl(code);
            RedirectView redirectView = new RedirectView();
            redirectView.setStatusCode(HttpStatus.FOUND);
            redirectView.setUrl(originalUrl);
            return redirectView;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No URL found for code: " + code
            );
        }
    }

}
