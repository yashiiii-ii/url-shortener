package com.yashi.shortener.dto;

import com.yashi.shortener.model.SafetyStatus;

import java.time.Instant;

public record CachedUrl(Long id, String originalUrl, String mobileUrl,
                        Instant expiresAt, SafetyStatus safetyStatus) {
}