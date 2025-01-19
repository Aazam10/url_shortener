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
import jakarta.persistence.EntityManager;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UrlService {

    private static final String BASE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_CODE_LENGTH = 7;
    private final UrlRepository urlRepository;

    private final UserRepository userRepository;

    private final EntityManager entityManager;



    @Value("${batch.size:50}")
    private int batchSize;

    public UrlService(UrlRepository urlRepository,UserRepository userRepository
                      ,EntityManager entityManager){
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;

    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShortCodeResponseDto createShortUrl(OriginalUrlDto urlDto, String apiKey){

            User user = getUserByApiKey(apiKey);

            String shortCode = getShortCode(urlDto);
//            Integer id = sequenceService.getNextSequenceValue();
//        System.out.println(" id is " + id);
            UrlModel urlModel = createUrlModel(urlDto,shortCode,user);

            urlRepository.save(urlModel);
            ShortCodeResponseDto shortCodeResponseDto = new ShortCodeResponseDto();
            shortCodeResponseDto.setOriginalUrl(urlDto.getUrl());
            shortCodeResponseDto.setShortCode(shortCode);
            shortCodeResponseDto.setExpiryDate(urlDto.getExpiryDate());

            return shortCodeResponseDto;

    }

    @Transactional
    public List<ShortCodeResponseDto> createBulkShortCodes(List<OriginalUrlDto> urls, String apiKey) {

        List<ShortCodeResponseDto> shortCodeResponseDtoList = new ArrayList<>();

        List<UrlModel> urlsToSave = new ArrayList<>();

        User user = getUserByApiKey(apiKey);

        Set<String> generatedCodes = new HashSet<>();
        // Pre-generate and validate all short codes


        for(OriginalUrlDto url :urls){
            String shortCode = generateUniqueShortCodeWithRetry(url,generatedCodes);
            generatedCodes.add(shortCode);
            System.out.println(" original url "+ url.getUrl() + " short code " + shortCode);
           UrlModel urlModel = createUrlModel(url,shortCode,user);

            urlsToSave.add(urlModel);


           ShortCodeResponseDto shortCodeResponseDto = new ShortCodeResponseDto();

           shortCodeResponseDto.setOriginalUrl(url.getUrl());
           shortCodeResponseDto.setShortCode(shortCode);
           shortCodeResponseDto.setExpiryDate(url.getExpiryDate());
           shortCodeResponseDtoList.add(shortCodeResponseDto);


           if(urlsToSave.size() >= batchSize){
               saveBatchSafely(urlsToSave);
               urlsToSave.clear();
           }
        }
        if (!urlsToSave.isEmpty()) {
            saveBatchSafely(urlsToSave);
        }
        return shortCodeResponseDtoList;
    }

    private void saveBatchSafely(List<UrlModel> batch) {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                String insertSql = "INSERT INTO url_shortener (original_url, short_code, " +
                        "num_visited, user_id, expiry_date) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    connection.setAutoCommit(false);

                    for (UrlModel url : batch) {
                        pstmt.setString(1, url.getOriginalUrl());
                        pstmt.setString(2, url.getShortCode());
                        pstmt.setInt(3, url.getNumVisited());
                        pstmt.setInt(4, url.getUser().getId());
                        pstmt.setString(5, url.getExpiryDate() != null ? url.getExpiryDate().toString() : null);

                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                    connection.commit();
                }
            });

            entityManager.clear();
        } catch (Exception e) {
            System.out.println("Error saving batch: "+ e);
            throw new RuntimeException("Failed to save URL batch", e);
        }
    }

    private String generateUniqueShortCodeWithRetry(OriginalUrlDto urlDto, Set<String> existingCodes) {
        String shortCode;
        int attempts = 0;
        do {
            if (hasCustomShortCode(urlDto)) {
                shortCode = urlDto.getShortCode().trim();
                if (validateShortCodeUniqueness(shortCode)) {
                    throw new DuplicateShortCodeException("Custom short code already exists", HttpStatus.BAD_REQUEST);
                }
            } else {
                shortCode = generateUniqueShortCode(urlDto.getUrl() + attempts);
            }
            attempts++;

            if (attempts > 5) {
                throw new RuntimeException("Failed to generate unique short code after 5 attempts");
            }
        } while (existingCodes.contains(shortCode) || validateShortCodeUniqueness(shortCode));

        return shortCode;
    }

    private User getUserByApiKey(String apiKey){
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new NoSuchUserFoundException(
                        "No user with this api key is present",
                        HttpStatus.NOT_FOUND));
    }


    private String getShortCode(OriginalUrlDto urlDto){
        if(hasCustomShortCode(urlDto)){
            String shortCode = urlDto.getShortCode().trim();
            if(validateShortCodeUniqueness(shortCode)){
                throw new DuplicateShortCodeException(" The short code provided by you already exists",HttpStatus.BAD_REQUEST);
            }
            return shortCode;
        }
        return generateUniqueShortCode(urlDto.getUrl());
    }

    private boolean validateShortCodeUniqueness(String shortCode){
        return urlRepository.findByShortCode(shortCode).isPresent();
    }

    private boolean hasCustomShortCode(OriginalUrlDto urlDto){
        return urlDto.getShortCode() != null && !urlDto.getShortCode().trim().isEmpty();
    }

    private UrlModel createUrlModel(OriginalUrlDto urlDto,String shortCode,User user){
        UrlModel urlModel = new UrlModel();
//        urlModel.setId(id);
        urlModel.setOriginalUrl(urlDto.getUrl());
        urlModel.setShortCode(shortCode);
        urlModel.setUser(user);
        urlModel.setExpiryDate(urlDto.getExpiryDate());
        urlModel.setNumVisited(0);
        return urlModel;
    }

    public Optional<UrlModel> getOriginalUrl(String shortCode){
        Optional<UrlModel> url = urlRepository.findByShortCode(shortCode);
//add expiry check
        if(url.isPresent()){
            if(url.get().getDeletedOn() == null){
                url.get().setNumVisited(url.get().getNumVisited() + 1);
                urlRepository.save(url.get());
            }else{
                return Optional.empty();
            }

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
        url.get().setDeletedOn(LocalDateTime.now());
        urlRepository.save(url.get());

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
