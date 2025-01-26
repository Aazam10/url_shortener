package com.example.url_shortner.controller;


import com.example.url_shortner.dto.*;
import com.example.url_shortner.exceptions.*;
import com.example.url_shortner.model.UrlModel;
import com.example.url_shortner.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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
    public ResponseEntity<UrlDetailsResponseDto> createShortUrl(@RequestHeader(name = "x-api-key")String api_key,
                                            @Valid @RequestBody CreateUrlRequestDto createUrlRequestDto){
//        System.out.println(" short code is " + createUrlRequestDto.getShortCode());
//        System.out.println(" expiry date "+ createUrlRequestDto.getExpiryDate());
        UrlDetailsResponseDto responseDto = urlService.createShortUrl(createUrlRequestDto,api_key);
        return new ResponseEntity<>(responseDto,HttpStatus.CREATED);
    }

    @GetMapping("/redirect")
    public Object redirectShortUrl(@RequestParam("code") String code){
        System.out.println(" code " + code);
        Optional<UrlModel> urlModel =urlService.getOriginalUrl(code);

        if(urlModel.isEmpty()){
            throw new ResourceNotFoundException("No url exists mapped to this short code " + code,HttpStatus.NOT_FOUND);
        }

        if(urlModel.get().getPassword() != null) {
            ModelAndView modelAndView = new ModelAndView("password-form");
            modelAndView.addObject("shortCode", code);
            return modelAndView;
        }

        urlService.incrementVisits(urlModel.get());
        RedirectView redirectView = new RedirectView();
        redirectView.setStatusCode(HttpStatus.FOUND);
        redirectView.setUrl(urlModel.get().getOriginalUrl());
        return redirectView;

    }
    @PostMapping("/validate-password")
    public RedirectView validateAndRedirect(
            @RequestParam("code") String code,
            @RequestParam("password") String password
    ) {
        Optional<UrlModel> urlModel = urlService.getOriginalUrl(code);

        if(urlModel.isEmpty()){
            throw new ResourceNotFoundException("No url exists mapped to this short code " + code,HttpStatus.NOT_FOUND);
        }
        UrlModel url = urlModel.get();
        if (url.getPassword() != null) {
            if (password == null || !urlService.validatePassword(password, url.getPassword())) {
                throw new NotAuthorizedException("Invalid password for protected URL", HttpStatus.UNAUTHORIZED);
            }
        }

        urlService.incrementVisits(urlModel.get());

        RedirectView redirectView = new RedirectView();
        redirectView.setStatusCode(HttpStatus.FOUND);
        redirectView.setUrl(urlModel.get().getOriginalUrl());
        return redirectView;
    }


    @DeleteMapping("/url/{code}")
    public ResponseEntity<?> deleteUrl(@RequestHeader(name="x-api-key") String apiKey,
                                       @PathVariable(value = "code") String code){
        urlService.deleteUrl(code,apiKey);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/url/{code}/status")
    public UrlDetailsResponseDto makeUrlInactive(@RequestHeader(name="x-api-key") String apiKey,
                                             @PathVariable(value="code") String code,
                                             @RequestBody InactiveUrlRequestDto inactiveUrlRequestDto){
        System.out.println(" here in put");
        return urlService.makeUrlInactive(code,apiKey,inactiveUrlRequestDto.getExpiryDate());
    }

    @PutMapping("/url/{code}/security")
    public UrlDetailsResponseDto updateUrlPassword(@RequestHeader(name = "x-api-key") String apiKey,
                                                   @PathVariable(value = "code") String shortCode,
                                                   @RequestBody UpdateUrlPasswordDto updateUrlPasswordDto) {

        return urlService.updateUrlPassword(apiKey,shortCode,updateUrlPasswordDto);

    }

    @PostMapping("/shorten/bulk")
    public ResponseEntity<?> createUrlInBulk(@RequestHeader(name = "x-api-key") String apiKey,
                                             @RequestBody BulkUrlRequestDto urls){
        //do something
        List<UrlDetailsResponseDto> shortCodes = urlService.createBulkShortCodes(urls.getUrls(),apiKey);
        return new ResponseEntity<>(shortCodes,HttpStatus.CREATED);
    }

    @GetMapping("/urls/{userId}")
    public ResponseEntity<?> getAllUrlsForUser(@PathVariable(name = "userId") Integer userId){
        System.out.println(" user Id is " + userId);
        List<UrlDetailsResponseDto> urls = urlService.getAllUrlsForUser(userId);
        System.out.println(urls);
        return new ResponseEntity<>(urls,HttpStatus.OK);
    }



}