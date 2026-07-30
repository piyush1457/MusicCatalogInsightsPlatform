package com.musiccatalog.controller;

import com.musiccatalog.config.AuthPrincipal;
import com.musiccatalog.dto.LibraryItemRequest;
import com.musiccatalog.dto.LibraryItemResponse;
import com.musiccatalog.dto.LibraryUpdateRequest;
import com.musiccatalog.dto.PagedResponse;
import com.musiccatalog.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LibraryItemResponse>> getLibrary(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String year) {

        PagedResponse<LibraryItemResponse> result =
                libraryService.getUserLibrary(principal.userId(), page, size, genre, year);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<LibraryItemResponse> createItem(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody LibraryItemRequest request) {

        LibraryItemResponse response = libraryService.createItem(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LibraryItemResponse> updateItem(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody LibraryUpdateRequest request) {

        LibraryItemResponse response = libraryService.updateItem(principal.userId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long id) {

        libraryService.deleteItem(principal.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
