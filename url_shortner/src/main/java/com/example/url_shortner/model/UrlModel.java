package com.example.url_shortner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UrlModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="original_url",nullable = false)
    private String originalUrl;

    @Column(name ="short_code",nullable = false,unique = true)
    private String shortCode;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime created_at;

}
