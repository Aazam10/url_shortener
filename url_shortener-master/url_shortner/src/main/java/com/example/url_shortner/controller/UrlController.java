package com.example.url_shortner.controller;


import com.example.url_shortner.dto.OriginalUrlDto;
import com.example.url_shortner.exceptions.EmptyUrlException;
import com.example.url_shortner.exceptions.ResourceNotFoundException;
import com.example.url_shortner.model.UrlModel;
import com.example.url_shortner.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;


@RestController

public class UrlController {

    private final UrlService urlService;

    public  UrlController(UrlService urlService){
        this.urlService = urlService;
    }
    @PostMapping(path = "/shorten")
    public ResponseEntity<?> createShortUrl(@RequestBody OriginalUrlDto originalUrlDto){
        String url = originalUrlDto.getUrl();

        if(url.trim().isEmpty()){
           throw new EmptyUrlException("Requested url to shorten cannot bes Empty",HttpStatus.BAD_REQUEST);
        }
        return urlService.createShortUrl(url);
    }

    @GetMapping("/redirect")
    public RedirectView redirectShortUrl(@RequestParam("code") String code){

        Optional<UrlModel> urlModel =urlService.getOriginalUrl(code);

        if(urlModel.isEmpty()){
            throw new ResourceNotFoundException("No url exists mapped to this short code " + code,HttpStatus.NOT_FOUND);
        }


        RedirectView redirectView = new RedirectView();
        redirectView.setStatusCode(HttpStatus.FOUND);
        redirectView.setUrl(urlModel.get().getOriginalUrl());
        return redirectView;

    }

    @DeleteMapping("/url/{code}")

    public ResponseEntity<?> deleteUrl(@PathVariable(value = "code") String code) throws ResourceNotFoundException{
        System.out.println(" code is " + code);


        urlService.deleteUrl(code);
        return ResponseEntity.noContent().build();

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(EmptyUrlException.class)
    public ResponseEntity<String> handleEmptyUrlException(EmptyUrlException ex){
        return ResponseEntity
                .status(ex.statusCode)
                .body(ex.getMessage());
    }
}