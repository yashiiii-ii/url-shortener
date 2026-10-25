package com.yashi.shortener.service;

import com.yashi.shortener.dto.CachedUrl;
import com.yashi.shortener.dto.ShortenRequest;
import com.yashi.shortener.dto.ShortenResponse;
import com.yashi.shortener.model.SafetyStatus;
import com.yashi.shortener.model.Url;
import com.yashi.shortener.repository.UrlRepository;
import com.yashi.shortener.util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UrlService {

    private final UrlRepository repo;
    private final SafetyChecker safetyChecker;

    @Value("${app.base-url}")
    private String baseUrl;

    public UrlService(UrlRepository repo, SafetyChecker safetyChecker) {
        this.repo = repo;
        this.safetyChecker = safetyChecker;
    }

    @Transactional
    public ShortenResponse shorten(ShortenRequest req) {
        SafetyStatus status = safetyChecker.check(req.url());
        if (status == SafetyStatus.BLOCKED) {
            throw new IllegalArgumentException("Link blocked: looks malicious or invalid");
        }

        Url u = new Url();
        u.setOriginalUrl(req.url());
        u.setMobileUrl(req.mobileUrl());
        u.setExpiresAt(req.expiresAt());
        u.setSafetyStatus(status);

        boolean hasAlias = req.customAlias() != null && !req.customAlias().isBlank();
        if (hasAlias) {
            if (repo.existsByShortCode(req.customAlias())) {
                throw new IllegalArgumentException("Alias already taken");
            }
            u.setShortCode(req.customAlias());
            repo.save(u);
        } else {
            u.setShortCode(UUID.randomUUID().toString().substring(0, 12)); // temporary
            repo.save(u);
            u.setShortCode(Base62.encode(u.getId() + 100000));             // final code
            repo.save(u);
        }

        return new ShortenResponse(baseUrl + "/" + u.getShortCode(), u.getShortCode(), status);
    }

    public CachedUrl resolve(String code) {
        Url u = repo.findByShortCode(code)
                .orElseThrow(() -> new NoSuchElementException("Short link not found"));
        return new CachedUrl(u.getId(), u.getOriginalUrl(), u.getMobileUrl(),
                u.getExpiresAt(), u.getSafetyStatus());
    }
}