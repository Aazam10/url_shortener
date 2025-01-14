package com.example.url_shortner.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String email;


    private String name;

    @Column(unique = true,name = "api_key",nullable = false)
    private String apiKey;

    @Column(name="created_at",nullable = false,updatable = false,
             columnDefinition = "TEXT DEFAULT (strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime'))")
    private LocalDateTime createdAt;

}
