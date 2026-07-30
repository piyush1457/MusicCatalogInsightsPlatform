package com.musiccatalog.service;

import com.musiccatalog.dto.LibraryItemRequest;
import com.musiccatalog.dto.LibraryItemResponse;
import com.musiccatalog.dto.LibraryUpdateRequest;
import com.musiccatalog.dto.PagedResponse;
import com.musiccatalog.entity.LibraryItem;
import com.musiccatalog.repository.LibraryItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class LibraryService {

    private final LibraryItemRepository repository;

    public LibraryService(LibraryItemRepository repository) {
        this.repository = repository;
    }

    public PagedResponse<LibraryItemResponse> getUserLibrary(
            Long userId, int page, int size, String genre, String year) {

        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<LibraryItem> items;
        if ((genre == null || genre.isBlank()) && (year == null || year.isBlank())) {
            items = repository.findByUserId(userId, pageable);
        } else {
            String g = (genre == null || genre.isBlank()) ? null : genre;
            String y = (year == null || year.isBlank()) ? null : year;
            items = repository.findByUserIdWithFilters(userId, g, y, pageable);
        }

        var content = items.getContent().stream()
                .map(LibraryItemResponse::fromEntity)
                .collect(Collectors.toList());

        return new PagedResponse<>(items, content);
    }

    public LibraryItemResponse createItem(Long userId, LibraryItemRequest request) {
        LibraryItem item = new LibraryItem();
        item.setUserId(userId);
        item.setAppleCatalogId(request.getAppleCatalogId());
        item.setTitle(request.getTitle());
        item.setArtistName(request.getArtistName());
        item.setGenre(request.getGenre());
        item.setReleaseDate(request.getReleaseDate());
        item.setDurationSeconds(request.getDurationSeconds());
        item.setArtworkUrl(request.getArtworkUrl());

        item = repository.save(item);
        return LibraryItemResponse.fromEntity(item);
    }

    public LibraryItemResponse updateItem(Long userId, Long itemId, LibraryUpdateRequest request) {
        LibraryItem item = repository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Library item not found or does not belong to user"));

        if (request.getUserRating() != null) {
            item.setUserRating(request.getUserRating());
        }
        if (request.getUserNotes() != null) {
            item.setUserNotes(request.getUserNotes());
        }

        item = repository.save(item);
        return LibraryItemResponse.fromEntity(item);
    }

    public void deleteItem(Long userId, Long itemId) {
        LibraryItem item = repository.findByIdAndUserId(itemId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Library item not found or does not belong to user"));
        repository.delete(item);
    }
}
