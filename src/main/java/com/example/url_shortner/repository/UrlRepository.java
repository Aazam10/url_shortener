package com.example.url_shortner.repository;



import com.example.url_shortner.model.UrlModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlModel,Integer> {

    Optional<UrlModel> findByShortCode(String shortCode);

    Optional<UrlModel> findByOriginalUrl(String originalUrl);
    void deleteByShortCode(String shortCode);

    List<UrlModel> findByUserIdAndDeletedOnIsNullAndExpiryDateIsNullOrExpiryDateGreaterThan(
            Integer userId,
            LocalDateTime currentTime
    );
}

