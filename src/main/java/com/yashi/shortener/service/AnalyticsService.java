package com.yashi.shortener.service;

import com.yashi.shortener.model.ClickEvent;
import com.yashi.shortener.repository.ClickEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ClickEventRepository repo;

    public AnalyticsService(ClickEventRepository repo) {
        this.repo = repo;
    }

    @Async
    public void recordClick(Long urlId, String userAgent, String referrer) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();

        // bot filter: bots ke clicks count nahi honge
        if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) return;

        ClickEvent e = new ClickEvent();
        e.setUrlId(urlId);
        e.setDevice(isMobile(ua) ? "MOBILE" : "DESKTOP");
        e.setBrowser(browser(ua));
        e.setReferrer(referrer);
        repo.save(e);
    }

    public static boolean isMobile(String ua) {
        return ua.contains("mobile") || ua.contains("android") || ua.contains("iphone");
    }

    private String browser(String ua) {
        if (ua.contains("edg")) return "Edge";
        if (ua.contains("chrome")) return "Chrome";
        if (ua.contains("firefox")) return "Firefox";
        if (ua.contains("safari")) return "Safari";
        return "Other";
    }

    public Map<String, Object> stats(Long urlId) {
        Map<String, Long> devices = new HashMap<>();
        for (Object[] r : repo.countByDevice(urlId)) {
            devices.put((String) r[0], (Long) r[1]);
        }
        Map<String, Long> browsers = new HashMap<>();
        for (Object[] r : repo.countByBrowser(urlId)) {
            browsers.put((String) r[0], (Long) r[1]);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalClicks", repo.countByUrlId(urlId));
        result.put("devices", devices);
        result.put("browsers", browsers);
        return result;
    }
}