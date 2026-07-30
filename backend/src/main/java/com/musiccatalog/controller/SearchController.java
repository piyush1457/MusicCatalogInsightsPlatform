package com.musiccatalog.controller;

import com.musiccatalog.dto.SearchResultDto;
import com.musiccatalog.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<SearchResultDto>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "song") String type,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<SearchResultDto> results = searchService.search(query, type, limit);
        return ResponseEntity.ok(results);
    }
}
