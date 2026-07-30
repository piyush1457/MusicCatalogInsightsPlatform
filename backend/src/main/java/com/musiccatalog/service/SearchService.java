package com.musiccatalog.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.musiccatalog.dto.ITunesResponse;
import com.musiccatalog.dto.SearchResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final RestTemplate restTemplate;
    private final Cache<String, List<SearchResultDto>> searchCache;
    private final String searchUrl;

    public SearchService(RestTemplate restTemplate,
                         Cache<String, List<SearchResultDto>> searchCache,
                         @Value("${itunes.search-url}") String searchUrl) {
        this.restTemplate = restTemplate;
        this.searchCache = searchCache;
        this.searchUrl = searchUrl;
    }

    public List<SearchResultDto> search(String query, String type, Integer limit) {
        if (limit == null || limit < 1) limit = 10;
        if (limit > 50) limit = 50;
        if (type == null || type.isBlank()) type = "song";

        String cacheKey = buildCacheKey(query, type, limit);
        List<SearchResultDto> cached = searchCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        String url = UriComponentsBuilder.fromHttpUrl(searchUrl)
                .queryParam("term", query)
                .queryParam("media", "music")
                .queryParam("entity", type)
                .queryParam("limit", limit)
                .build()
                .toUriString();

        log.debug("Calling iTunes API: {}", url);

        try {
            ITunesResponse response = restTemplate.getForObject(url, ITunesResponse.class);

            if (response == null || response.getResults() == null) {
                return Collections.emptyList();
            }

            List<SearchResultDto> results = response.getResults().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());

            searchCache.put(cacheKey, results);
            return results;
        } catch (Exception e) {
            log.error("iTunes API call failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private SearchResultDto mapToDto(ITunesResponse.ITunesResult result) {
        SearchResultDto dto = new SearchResultDto();
        dto.setAppleCatalogId(result.getTrackId());
        dto.setTitle(result.getTrackName());
        dto.setArtistName(result.getArtistName());
        dto.setGenre(result.getPrimaryGenreName());

        if (result.getReleaseDate() != null) {
            dto.setReleaseDate(result.getReleaseDate().substring(0, 10));
        }

        if (result.getTrackTimeMillis() != null) {
            dto.setDurationSeconds((int) (result.getTrackTimeMillis() / 1000));
        }

        dto.setArtworkUrl(result.getArtworkUrl100());
        return dto;
    }

    private String buildCacheKey(String query, String type, int limit) {
        return query + "|" + type + "|" + limit;
    }
}
