package com.example.url_shortner.model;

import com.example.url_shortner.utils.TextBasedDateTimeConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true,nullable = false)
    private String email;

    private String name;

    @Column(unique = true,name = "api_key",nullable = false)
    private String apiKey;

    @Column(name="created_at",nullable = false,updatable = false,
             columnDefinition = "TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))")
    @Convert(converter = TextBasedDateTimeConverter.class)
    private LocalDateTime createdAt;

    @Column(name = "tier",nullable = false)
    private String tier;
//    @OneToMany(
//            mappedBy = "user",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true,
//            fetch = FetchType.LAZY
//    )
//    private Set<UrlModel> urls = new HashSet<>();

}
