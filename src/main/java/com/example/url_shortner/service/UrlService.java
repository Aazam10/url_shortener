package com.example.url_shortner.service;


import com.example.url_shortner.dto.OriginalUrlDto;
import com.example.url_shortner.dto.ShortCodeResponseDto;
import com.example.url_shortner.exceptions.NoSuchUserFoundException;
import com.example.url_shortner.exceptions.ResourceNotFoundException;
import com.example.url_shortner.exceptions.DuplicateShortCodeException;
import com.example.url_shortner.model.UrlModel;
import com.example.url_shortner.model.User;
import com.example.url_shortner.repository.UrlRepository;
import com.example.url_shortner.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class UrlService {

    private static final String BASE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_CODE_LENGTH = 7;
    private final UrlRepository urlRepository;

    private final UserRepository userRepository;

    public UrlService(UrlRepository urlRepository,UserRepository userRepository){
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseEntity<?> createShortUrl(OriginalUrlDto urlDto, String apiKey){

        Optional<User> user = userRepository.findByApiKey(apiKey);

        if(user.isEmpty()){
            throw new NoSuchUserFoundException("No user with this api key is present",HttpStatus.NOT_FOUND);
        }

        try{

            if(urlDto.getShortCode() != null && !urlDto.getShortCode().trim().isEmpty()){
                String short_code = urlDto.getShortCode().trim();
                Optional<UrlModel> existingUrlModel = urlRepository.findByShortCode(short_code);

                if(existingUrlModel.isPresent()){
                    throw new DuplicateShortCodeException(" The short code provided by you already exists",HttpStatus.BAD_REQUEST);
                }else{
                    String url = urlDto.getUrl();


                    UrlModel urlModel = new UrlModel();

                    urlModel.setOriginalUrl(url);
                    urlModel.setShortCode(short_code);
                    urlModel.setNumVisited(0);
                    urlModel.setUser(user.get());
                    urlModel.setExpiryDate(urlDto.getExpiryDate());

                    urlRepository.save(urlModel);

                    return new ResponseEntity<ShortCodeResponseDto>(new ShortCodeResponseDto(short_code), HttpStatus.CREATED);
                }
            }else{
                String url = urlDto.getUrl();
                String short_code = generateUniqueShortCode(url);

                UrlModel urlModel = new UrlModel();

                urlModel.setOriginalUrl(url);
                urlModel.setShortCode(short_code);
                urlModel.setNumVisited(0);
                urlModel.setUser(user.get());
                urlModel.setExpiryDate(urlDto.getExpiryDate());

                urlRepository.save(urlModel);

                return new ResponseEntity<ShortCodeResponseDto>(new ShortCodeResponseDto(short_code), HttpStatus.CREATED);
            }


        }catch(DataAccessException e){
            return new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public Optional<UrlModel> getOriginalUrl(String shortCode){
        Optional<UrlModel> url = urlRepository.findByShortCode(shortCode);

        if(url.isPresent()){
            url.get().setNumVisited(url.get().getNumVisited() + 1);
            urlRepository.save(url.get());
        }

        return url;
    }

    @Transactional
    public String makeUrlInactive(String shortCode, String apiKey, LocalDateTime expiryDate){
        Optional<User> user = userRepository.findByApiKey(apiKey);

        if(user.isEmpty()){
            throw new NoSuchUserFoundException(" No user with this api key is found " ,HttpStatus.BAD_REQUEST);
        }

        if(apiKey!= null && !apiKey.equals(user.get().getApiKey())){
            throw new RuntimeException(" Not authorized to change the url " + HttpStatus.UNAUTHORIZED);
        }

        Optional<UrlModel> existingUrlModel = urlRepository.findByShortCode(shortCode);

        if(existingUrlModel.isEmpty()){
            throw new ResourceNotFoundException(" No url with short code " + shortCode + " is present" , HttpStatus.BAD_REQUEST);
        }

        existingUrlModel.get().setExpiryDate(expiryDate);

        urlRepository.save(existingUrlModel.get());

        return "success";
    }

    @Transactional
    public void deleteUrl(String shortCode,String api_key){
        Optional<UrlModel> url = urlRepository.findByShortCode(shortCode);

        if(url.isEmpty()){
            throw new ResourceNotFoundException(" No code with " + shortCode + " value exists " , HttpStatus.NOT_FOUND);
        }
        Optional<User> user = userRepository.findById(url.get().getUser().getId());

        if(user.isEmpty()){
            throw new NoSuchUserFoundException(" No user found " , HttpStatus.NOT_FOUND);
        }

        if(!user.get().getApiKey().equals(api_key)){
            throw new NoSuchUserFoundException(" The api key is not associated with the owner of code " , HttpStatus.BAD_REQUEST);
        }

        urlRepository.deleteByShortCode(shortCode);
    }

    private String generateUniqueShortCode(String originalUrl) {
        try {
            // Generate MD5 hash of the URL
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(originalUrl.getBytes());

            // Convert to base64 and extract characters
            String base64Hash = Base64.getUrlEncoder().encodeToString(hashBytes);

            // Generate short code
            StringBuilder shortCode = new StringBuilder();
            int attempts = 0;

            while (shortCode.length() < SHORT_CODE_LENGTH) {
                // Use hash characters and mix in some randomness
                char nextChar = base64Hash.charAt(attempts % base64Hash.length());

                // Ensure we're using valid characters
                if (BASE_CHARS.indexOf(nextChar) != -1) {
                    shortCode.append(nextChar);
                }
                attempts++;

                // Prevent infinite loop and handle collisions
                if (attempts > 100) {
                    nextChar = BASE_CHARS.charAt(attempts % BASE_CHARS.length());
                    shortCode.append(nextChar);
                }
            }

            String finalShortCode = shortCode.toString().substring(0, SHORT_CODE_LENGTH);

//             Ensure unique code
            while (!urlRepository.findByShortCode(finalShortCode).isEmpty()) {
                finalShortCode = regenerateShortCode(finalShortCode);
            }

            return finalShortCode;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating short code", e);
        }
    }

    private String regenerateShortCode(String existingCode) {
        // Simple collision resolution by appending a character
        return existingCode + BASE_CHARS.charAt(
                (existingCode.hashCode() & 0xFF) % BASE_CHARS.length()
        );
    }



}
