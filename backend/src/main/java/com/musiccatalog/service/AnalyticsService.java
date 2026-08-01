package com.musiccatalog.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.musiccatalog.dto.AnalyticsSummary;
import com.musiccatalog.entity.LibraryItem;
import com.musiccatalog.repository.LibraryItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final LibraryItemRepository libraryItemRepository;
    private final WebClient webClient;
    private final Cache<String, AnalyticsSummary> analyticsCache;

    @Value("${groq.api-key}")
    private String groqApiKey;

    public AnalyticsService(LibraryItemRepository libraryItemRepository,
                             WebClient.Builder webClientBuilder,
                             Cache<String, AnalyticsSummary> analyticsCache) {
        this.libraryItemRepository = libraryItemRepository;
        this.webClient = webClientBuilder.build();
        this.analyticsCache = analyticsCache;
    }

    public AnalyticsSummary computeSummary(Long userId) {
        List<LibraryItem> items = libraryItemRepository.findAllByUserId(userId);

        if (items.isEmpty()) {
            AnalyticsSummary empty = new AnalyticsSummary();
            empty.setTotalItems(0);
            empty.setGenreDistribution(Collections.emptyList());
            empty.setReleasesByYear(Collections.emptyList());
            empty.setTopArtists(Collections.emptyList());
            AnalyticsSummary.DurationHistogram dh = new AnalyticsSummary.DurationHistogram();
            empty.setDurationHistogram(dh);
            return empty;
        }

        Map<String, Long> genreCounts = items.stream()
                .filter(i -> i.getGenre() != null)
                .collect(Collectors.groupingBy(LibraryItem::getGenre, Collectors.counting()));
        List<AnalyticsSummary.GenreCount> genreDist = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new AnalyticsSummary.GenreCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Map<String, Long> yearCounts = items.stream()
                .filter(i -> i.getReleaseDate() != null && i.getReleaseDate().length() >= 4)
                .collect(Collectors.groupingBy(
                        i -> i.getReleaseDate().substring(0, 4),
                        Collectors.counting()));
        List<AnalyticsSummary.YearCount> years = yearCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByKey())
                .map(e -> new AnalyticsSummary.YearCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Map<String, Long> artistCounts = items.stream()
                .collect(Collectors.groupingBy(LibraryItem::getArtistName, Collectors.counting()));
        List<AnalyticsSummary.ArtistCount> topArtists = artistCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> new AnalyticsSummary.ArtistCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        AnalyticsSummary.DurationHistogram dh = new AnalyticsSummary.DurationHistogram();
        for (LibraryItem item : items) {
            if (item.getDurationSeconds() == null) continue;
            int sec = item.getDurationSeconds();
            if (sec < 120) dh.setBucket0to2(dh.getBucket0to2() + 1);
            else if (sec < 180) dh.setBucket2to3(dh.getBucket2to3() + 1);
            else if (sec < 240) dh.setBucket3to4(dh.getBucket3to4() + 1);
            else if (sec < 300) dh.setBucket4to5(dh.getBucket4to5() + 1);
            else dh.setBucket5plus(dh.getBucket5plus() + 1);
        }

        AnalyticsSummary summary = new AnalyticsSummary();
        summary.setTotalItems(items.size());
        summary.setGenreDistribution(genreDist);
        summary.setReleasesByYear(years);
        summary.setTopArtists(topArtists);
        summary.setDurationHistogram(dh);
        return summary;
    }

    public String generateAIInsight(String summaryJson) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return "AI insights are not available — the Groq API key is not configured.";
        }

        String prompt = "Analyze this music library analytics JSON and write a 3-5 sentence natural-language paragraph summarizing key trends. Mention dominant genres, decade/era leanings, average track length, and notable top artists. Keep it concise and conversational. JSON: " + summaryJson;

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.3-70b-versatile");
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "You are a helpful music analyst."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 300);

            String url = "https://api.groq.com/openai/v1/chat/completions";

            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + groqApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(30));

            if (response == null) {
                return "Groq returned an empty response. Please try again.";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "Groq returned no choices. Please try again.";
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            return "AI insights are temporarily unavailable. Please try again later.";
        }
    }
}
