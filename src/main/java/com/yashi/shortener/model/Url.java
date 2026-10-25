package com.yashi.shortener.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(length = 2048)
    private String mobileUrl;

    private Instant createdAt = Instant.now();

    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    private SafetyStatus safetyStatus;
}