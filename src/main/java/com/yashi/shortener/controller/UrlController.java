package com.yashi.shortener.controller;

import com.yashi.shortener.dto.CachedUrl;
import com.yashi.shortener.dto.ShortenRequest;
import com.yashi.shortener.dto.ShortenResponse;
import com.yashi.shortener.model.SafetyStatus;
import com.yashi.shortener.service.AnalyticsService;
import com.yashi.shortener.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final AnalyticsService analytics;

    public UrlController(UrlService urlService, AnalyticsService analytics) {
        this.urlService = urlService;
        this.analytics = analytics;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest req) {
        return ResponseEntity.status(201).body(urlService.shorten(req));
    }

    @GetMapping("/api/urls/{code}/analytics")
    public Map<String, Object> stats(@PathVariable String code) {
        CachedUrl c = urlService.resolve(code);
        return analytics.stats(c.id());
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code,
                                      @RequestParam(defaultValue = "false") boolean confirm,
                                      @RequestHeader(value = "User-Agent", required = false) String ua,
                                      @RequestHeader(value = "Referer", required = false) String referer) {
        CachedUrl c = urlService.resolve(code);

        if (c.expiresAt() != null && c.expiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("This link has expired");
        }

        if (c.safetyStatus() == SafetyStatus.SUSPICIOUS && !confirm) {
            String html = "<h2>This link may be risky</h2><p>"
                    + HtmlUtils.htmlEscape(c.originalUrl())
                    + "</p><a href=\"/" + code + "?confirm=true\">Continue anyway</a>";
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
        }

        String uaLower = ua == null ? "" : ua.toLowerCase();
        String target = (c.mobileUrl() != null && !c.mobileUrl().isBlank()
                && AnalyticsService.isMobile(uaLower))
                ? c.mobileUrl() : c.originalUrl();

        analytics.recordClick(c.id(), ua, referer);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }
}