package com.yashi.shortener.service;

import com.yashi.shortener.model.SafetyStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Component
public class SafetyChecker {

    private static final Set<String> SHORTENERS =
            Set.of("bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd");

    private static final List<String> KEYWORDS =
            List.of("login-verify", "free-money", "claim-prize", "update-account", "password-reset");

    public SafetyStatus check(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception e) {
            return SafetyStatus.BLOCKED;
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (host == null || scheme == null
                || !(scheme.equals("http") || scheme.equals("https"))) {
            return SafetyStatus.BLOCKED;
        }

        host = host.toLowerCase();
        int score = 0;

        if (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) score += 2;
        if (host.split("\\.").length > 4) score += 1;
        if (SHORTENERS.contains(host)) score += 1;
        if (host.contains("xn--")) score += 1;

        String lower = url.toLowerCase();
        for (String k : KEYWORDS) {
            if (lower.contains(k)) score += 2;
        }

        if (score >= 4) return SafetyStatus.BLOCKED;
        if (score >= 2) return SafetyStatus.SUSPICIOUS;
        return SafetyStatus.SAFE;
    }
}