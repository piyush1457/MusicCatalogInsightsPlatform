package com.musiccatalog.controller;

import com.musiccatalog.config.AuthPrincipal;
import com.musiccatalog.dto.AnalyticsSummary;
import com.musiccatalog.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummary> getSummary(
            @AuthenticationPrincipal AuthPrincipal principal) {

        AnalyticsSummary summary = analyticsService.computeSummary(principal.userId());
        return ResponseEntity.ok(summary);
    }
}
