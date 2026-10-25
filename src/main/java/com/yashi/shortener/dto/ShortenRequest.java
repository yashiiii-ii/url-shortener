package com.yashi.shortener.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record ShortenRequest(
        @NotBlank String url,
        String mobileUrl,
        String customAlias,
        Instant expiresAt) {
}