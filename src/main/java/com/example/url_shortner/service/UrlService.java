package com.example.url_shortner.service;


import com.example.url_shortner.dto.CreateUrlRequestDto;
import com.example.url_shortner.dto.UpdateUrlPasswordDto;
import com.example.url_shortner.dto.UrlDetailsResponseDto;
import com.example.url_shortner.exceptions.NoSuchUserFoundException;

import com.example.url_shortner.exceptions.NotAuthorizedException;
import com.example.url_shortner.exceptions.ResourceNotFoundException;
import com.example.url_shortner.exceptions.DuplicateShortCodeException;
import com.example.url_shortner.model.UrlModel;
import com.example.url_shortner.model.User;
import com.example.url_shortner.repository.UrlRepository;
import com.example.url_shortner.repository.UserRepository;
import jakarta.persistence.EntityManager;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
    public UrlDetailsResponseDto createShortUrl(CreateUrlRequestDto urlDto, String apiKey){

            User user = getUserByApiKey(apiKey);
            String shortCode = getShortCode(urlDto);
            UrlModel urlModel = createUrlModel(urlDto,shortCode,user);

            urlModel  = urlRepository.save(urlModel);
            return  mapToUrlDetailsResponse(urlModel);

    }

    @Transactional
    public List<UrlDetailsResponseDto> createBulkShortCodes(List<CreateUrlRequestDto> urls, String apiKey) {

        List<UrlDetailsResponseDto> urlDetailsResponseDtoList = new ArrayList<>();

        List<UrlModel> urlsToSave = new ArrayList<>();

        User user = getUserByApiKey(apiKey);

        if(!user.getTier().equalsIgnoreCase("enterprise")){
            throw new NotAuthorizedException(" Please upgrade your plan to create urls in bulk",HttpStatus.FORBIDDEN);
        }

        Set<String> generatedCodes = new HashSet<>();
        // Pre-generate and validate all short codes


        for(CreateUrlRequestDto url :urls){
            String shortCode = generateUniqueShortCodeWithRetry(url,generatedCodes);
            generatedCodes.add(shortCode);
            System.out.println(" original url "+ url.getUrl() + " short code " + shortCode);
           UrlModel urlModel = createUrlModel(url,shortCode,user);

            urlsToSave.add(urlModel);



           urlDetailsResponseDtoList.add(mapToUrlDetailsResponse(urlModel));


           if(urlsToSave.size() >= batchSize){
               saveBatchSafely(urlsToSave);
               urlsToSave.clear();
           }
        }
        if (!urlsToSave.isEmpty()) {
            saveBatchSafely(urlsToSave);
        }
        return urlDetailsResponseDtoList;
    }

    @Transactional
    public UrlDetailsResponseDto updateUrlPassword(String apiKey, String shortCode,
                                                   UpdateUrlPasswordDto updateUrlPasswordDto) {
        UrlModel existingUrl = getUrlModelByShortCode(shortCode);
        validateUsersApiKey(apiKey,existingUrl.getUser());
        existingUrl.setPassword(updateUrlPasswordDto.getPassword());
       UrlModel urlModel = urlRepository.save(existingUrl);

        return mapToUrlDetailsResponse(urlModel);
    }
    @Transactional
    public UrlDetailsResponseDto makeUrlInactive(String shortCode, String apiKey, LocalDateTime expiryDate){

        UrlModel existingUrl = getUrlModelByShortCode(shortCode);
        validateUsersApiKey(apiKey,existingUrl.getUser());
        existingUrl.setExpiryDate(expiryDate);

         UrlModel url =  urlRepository.save(existingUrl);

        return mapToUrlDetailsResponse(url);
    }

    @Transactional
    public void deleteUrl(String shortCode,String apiKey){
        UrlModel url = getUrlModelByShortCode(shortCode);
        validateUsersApiKey(apiKey,url.getUser());
        url.setDeletedOn(LocalDateTime.now());
        urlRepository.save(url);
    }

    @Transactional(readOnly = true)
    public List<UrlDetailsResponseDto> getAllUrlsForUser(Integer userId){
        List<UrlModel> urlModels = urlRepository.findByUserIdAndDeletedOnIsNullAndExpiryDateIsNullOrExpiryDateGreaterThan(userId,LocalDateTime.now());

        List<UrlDetailsResponseDto> urlDetailsResponseDtoList = new ArrayList<>();

        for(UrlModel urlModel : urlModels ){
            urlDetailsResponseDtoList.add(mapToUrlDetailsResponse(urlModel));
        }

        return urlDetailsResponseDtoList;
    }

    private User getUserByApiKey(String apiKey){
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new NoSuchUserFoundException(
                        "No user with this api key is present",
                        HttpStatus.NOT_FOUND));
    }

    private UrlDetailsResponseDto mapToUrlDetailsResponse(UrlModel urlModel) {
        return UrlDetailsResponseDto.builder()
                .originalUrl(urlModel.getOriginalUrl())
                .shortCode(urlModel.getShortCode())
                .createdAt(urlModel.getCreatedAt())
                .lastVisitedAt(urlModel.getLastVisitedAt())
                .numVisited(urlModel.getNumVisited())
                .expiryDate(urlModel.getExpiryDate())
                .build();
    }

    private void validateUsersApiKey(String apiKey,User user){
        System.out.println(user.getApiKey() + " supplied key " + apiKey);
        if(!user.getApiKey().equals(apiKey)){
            throw new NotAuthorizedException(" Not authorized to change the url " , HttpStatus.UNAUTHORIZED);
        }
    }

    private UrlModel getUrlModelByShortCode(String shortCode){
        Optional<UrlModel> url = urlRepository.findByShortCode(shortCode);

        if(url.isEmpty() || url.get().getDeletedOn() != null){
            throw new ResourceNotFoundException(" No code with " + shortCode + " value exists " , HttpStatus.NOT_FOUND);
        }

        return url.get();
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

    private String generateUniqueShortCodeWithRetry(CreateUrlRequestDto urlDto, Set<String> existingCodes) {
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

    private String getShortCode(CreateUrlRequestDto urlDto){
        if(hasCustomShortCode(urlDto)){
            System.out.println(" here in provided short code");
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

    private boolean hasCustomShortCode(CreateUrlRequestDto urlDto){
        return urlDto.getShortCode() != null && !urlDto.getShortCode().trim().isEmpty();
    }

    private UrlModel createUrlModel(CreateUrlRequestDto urlDto, String shortCode, User user){
        UrlModel urlModel = new UrlModel();
        urlModel.setOriginalUrl(urlDto.getUrl());
        urlModel.setShortCode(shortCode);
        urlModel.setUser(user);
        urlModel.setExpiryDate(urlDto.getExpiryDate());
        urlModel.setNumVisited(0);
        urlModel.setPassword(urlDto.getPassword());
        return urlModel;
    }

    public Optional<UrlModel> getOriginalUrl(String shortCode) {
        Optional<UrlModel> url = urlRepository.findByShortCode(shortCode);


        if (url.isPresent() && url.get().getDeletedOn() == null && !isExpired(url.get())) {
            System.out.println(" url " + url.get());
            return url;
        } else {
            return Optional.empty();
        }

    }

    private boolean isExpired(UrlModel urlModel){
        return urlModel.getExpiryDate() != null && urlModel.getExpiryDate().isBefore(LocalDateTime.now());
    }

    public void incrementVisits(UrlModel url) {
        url.setNumVisited(url.getNumVisited() + 1);
        urlRepository.save(url);
    }

    public boolean validatePassword(String provided, String stored) {
        return provided.equals(stored);
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
