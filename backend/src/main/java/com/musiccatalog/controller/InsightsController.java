package com.musiccatalog.controller;

import com.musiccatalog.config.AuthPrincipal;
import com.musiccatalog.dto.AIInsightRequest;
import com.musiccatalog.dto.AnalyticsSummary;
import com.musiccatalog.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final AnalyticsService analyticsService;

    public InsightsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/summary")
    public ResponseEntity<?> getAIInsight(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody AIInsightRequest request) {

        if (request == null || request.getSummary() == null || request.getSummary().isBlank()) {
            AnalyticsSummary empty = analyticsService.computeSummary(principal.userId());
            if (empty.getTotalItems() == 0) {
                return ResponseEntity.ok(java.util.Map.of(
                        "insight", "Your library is empty. Save some songs to see AI-powered insights."
                ));
            }
        }

        String summary = (request != null && request.getSummary() != null)
                ? request.getSummary()
                : analyticsService.computeSummary(principal.userId()).toString();

        String insight = analyticsService.generateAIInsight(summary);
        return ResponseEntity.ok(java.util.Map.of("insight", insight));
    }
}
