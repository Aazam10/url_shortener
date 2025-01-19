package com.example.url_shortner.controller;


import com.example.url_shortner.dto.BulkUrlRequestDto;
import com.example.url_shortner.dto.InactiveUrlRequestDto;
import com.example.url_shortner.dto.OriginalUrlDto;
import com.example.url_shortner.dto.ShortCodeResponseDto;
import com.example.url_shortner.exceptions.*;
import com.example.url_shortner.model.UrlModel;
import com.example.url_shortner.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Optional;


@RestController
public class UrlController {

    private final UrlService urlService;

    public  UrlController(UrlService urlService){
        this.urlService = urlService;
    }
    @PostMapping(path = "/shorten")
    public ResponseEntity<?> createShortUrl(@RequestHeader(name = "x-api-key")String api_key,
                                            @RequestBody OriginalUrlDto originalUrlDto){
        String url = originalUrlDto.getUrl();

        System.out.println(" short code is " + originalUrlDto.getShortCode());

        if(url.trim().isEmpty()){
           throw new EmptyUrlException("Requested url to shorten cannot bes Empty",HttpStatus.BAD_REQUEST);
        }
        System.out.println(" expiry date "+ originalUrlDto.getExpiryDate());
        ShortCodeResponseDto responseDto = urlService.createShortUrl(originalUrlDto,api_key);
        return new ResponseEntity<>(responseDto,HttpStatus.CREATED);
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
    public ResponseEntity<?> deleteUrl(@RequestHeader(name="x-api-key") String apiKey,
                                       @PathVariable(value = "code") String code){
        System.out.println(" code is " + code);


        urlService.deleteUrl(code,apiKey);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/url/{code}")
    public String makeUrlInactive(@RequestHeader(name="x-api-key") String apiKey,
                                             @PathVariable(value="code") String code,
                                             @RequestBody InactiveUrlRequestDto inactiveUrlRequestDto){
        System.out.println(" here in put");
        return urlService.makeUrlInactive(code,apiKey,inactiveUrlRequestDto.getExpiryDate());
    }

    @PostMapping("/shorten/bulk")
    public ResponseEntity<?> createUrlInBulk(@RequestHeader(name = "x-api-key") String apiKey,
                                             @RequestBody BulkUrlRequestDto urls){
        //do something
        for(OriginalUrlDto url : urls.getUrls()){
            System.out.println(url.getUrl());
        }

        List<ShortCodeResponseDto> shortCodes = urlService.createBulkShortCodes(urls.getUrls(),apiKey);
        return new ResponseEntity<>(shortCodes,HttpStatus.CREATED);
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
    @ExceptionHandler(NoSuchUserFoundException.class)
    public ResponseEntity<String> handleUnkownUserException(NoSuchUserFoundException ex){
        return ResponseEntity
                .status(ex.statusCode)
                .body(ex.getMessage());
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(NotAuthorizedException ex){
        return new ResponseEntity<>(ex.getMessage(),ex.httpStatus);
    }

    @ExceptionHandler(DuplicateShortCodeException.class)
    public ResponseEntity<String> handleExistingShortCode(DuplicateShortCodeException ex){
        return new ResponseEntity<>(ex.getMessage(),ex.httpStatusCode);
    }
}