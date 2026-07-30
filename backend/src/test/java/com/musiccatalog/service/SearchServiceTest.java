package com.musiccatalog.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.musiccatalog.dto.ITunesResponse;
import com.musiccatalog.dto.SearchResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Cache<String, List<SearchResultDto>> searchCache;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService(restTemplate, searchCache, "https://itunes.apple.com/search");
    }

    @Test
    void shouldMapITunesResultToSearchResultDto() {
        ITunesResponse.ITunesResult result = new ITunesResponse.ITunesResult();
        result.setTrackId(123456L);
        result.setTrackName("Test Song");
        result.setArtistName("Test Artist");
        result.setPrimaryGenreName("Pop");
        result.setReleaseDate("2024-03-15T12:00:00Z");
        result.setTrackTimeMillis(240000L);
        result.setArtworkUrl100("https://example.com/artwork.jpg");

        SearchResultDto dto = mapResult(result);

        assertEquals(123456L, dto.getAppleCatalogId());
        assertEquals("Test Song", dto.getTitle());
        assertEquals("Test Artist", dto.getArtistName());
        assertEquals("Pop", dto.getGenre());
        assertEquals("2024-03-15", dto.getReleaseDate());
        assertEquals(240, dto.getDurationSeconds());
        assertEquals("https://example.com/artwork.jpg", dto.getArtworkUrl());
    }

    @Test
    void shouldMapResultWithNullReleaseDate() {
        ITunesResponse.ITunesResult result = new ITunesResponse.ITunesResult();
        result.setTrackId(1L);
        result.setTrackName("No Date");
        result.setArtistName("Artist");
        result.setReleaseDate(null);
        result.setTrackTimeMillis(null);

        SearchResultDto dto = mapResult(result);

        assertNull(dto.getReleaseDate());
        assertNull(dto.getDurationSeconds());
    }

    @Test
    void shouldMapResultWithZeroTrackTime() {
        ITunesResponse.ITunesResult result = new ITunesResponse.ITunesResult();
        result.setTrackId(2L);
        result.setTrackName("No Duration");
        result.setArtistName("Artist");
        result.setTrackTimeMillis(0L);

        SearchResultDto dto = mapResult(result);

        assertEquals(0, dto.getDurationSeconds());
    }

    private SearchResultDto mapResult(ITunesResponse.ITunesResult result) {
        SearchResultDto dto = new SearchResultDto();
        dto.setAppleCatalogId(result.getTrackId());
        dto.setTitle(result.getTrackName());
        dto.setArtistName(result.getArtistName());
        dto.setGenre(result.getPrimaryGenreName());

        if (result.getReleaseDate() != null && result.getReleaseDate().length() >= 10) {
            dto.setReleaseDate(result.getReleaseDate().substring(0, 10));
        }

        if (result.getTrackTimeMillis() != null) {
            dto.setDurationSeconds((int) (result.getTrackTimeMillis() / 1000));
        }

        dto.setArtworkUrl(result.getArtworkUrl100());
        return dto;
    }
}
