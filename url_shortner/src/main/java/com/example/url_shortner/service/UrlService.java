package com.example.url_shortner.service;

import com.example.url_shortner.dto.OriginalUrlDto;
import com.example.url_shortner.dto.ShortCodeResponseDto;
import com.example.url_shortner.repository.UrlRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class UrlService {

    private static final String BASE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_CODE_LENGTH = 7;
    private final UrlRepository urlRepository;

    public  UrlService(UrlRepository urlRepository){
        this.urlRepository = urlRepository;
    }

    public ResponseEntity<?> createShortUrl(String url){
        try{

        String short_code = generateUniqueShortCode(url);

         urlRepository.createUrlShortCode(url,short_code);

            return new ResponseEntity<ShortCodeResponseDto>(new ShortCodeResponseDto(short_code), HttpStatus.CREATED);
        }catch(DataAccessException e){
            return new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public String getOriginalUrl(String short_code){
        OriginalUrlDto mapping = urlRepository.findByCode(short_code);
        if (mapping == null) {
            throw new RuntimeException("URL not found for code: " + short_code);
        }
        return mapping.getUrl();
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
            while (urlRepository.findByCode(finalShortCode) != null) {
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
