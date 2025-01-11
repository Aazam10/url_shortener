package com.example.url_shortner.model;

import com.example.url_shortner.utils.TextBasedDateTimeConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "url_shortener")
public class UrlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="original_url",nullable = false)
    private String originalUrl;

    @Column(name ="short_code",nullable = false,unique = true)
    private String shortCode;

    @CreationTimestamp
    @Column(name="created_at",nullable = false,columnDefinition = "TEXT")
    @Convert(converter = TextBasedDateTimeConverter.class)
    private LocalDateTime created_at;


    @Column(name="num_visited",nullable = false)
    private Long numVisited;

    @UpdateTimestamp
    @Column(name="last_visited_at", nullable = false,columnDefinition = "TEXT")
    @Convert(converter = TextBasedDateTimeConverter.class)
    private LocalDateTime lastVisitedAt;

}
