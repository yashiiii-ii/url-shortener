package com.yashi.shortener.dto;

import com.yashi.shortener.model.SafetyStatus;

public record ShortenResponse(String shortUrl, String shortCode, SafetyStatus safetyStatus) {
}