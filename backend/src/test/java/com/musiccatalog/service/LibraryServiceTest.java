package com.musiccatalog.service;

import com.musiccatalog.dto.LibraryItemRequest;
import com.musiccatalog.dto.LibraryItemResponse;
import com.musiccatalog.dto.LibraryUpdateRequest;
import com.musiccatalog.dto.PagedResponse;
import com.musiccatalog.entity.LibraryItem;
import com.musiccatalog.repository.LibraryItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private LibraryItemRepository repository;

    private LibraryService service;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        service = new LibraryService(repository);
    }

    @Test
    void createItem_shouldSaveAndReturnResponse() {
        LibraryItemRequest request = new LibraryItemRequest();
        request.setAppleCatalogId(123L);
        request.setTitle("Test Song");
        request.setArtistName("Test Artist");
        request.setGenre("Pop");
        request.setDurationSeconds(200);

        LibraryItem savedItem = new LibraryItem();
        savedItem.setId(1L);
        savedItem.setUserId(userId);
        savedItem.setAppleCatalogId(123L);
        savedItem.setTitle("Test Song");
        savedItem.setArtistName("Test Artist");
        savedItem.setGenre("Pop");
        savedItem.setDurationSeconds(200);

        when(repository.save(any(LibraryItem.class))).thenReturn(savedItem);

        LibraryItemResponse response = service.createItem(userId, request);

        assertEquals("Test Song", response.getTitle());
        assertEquals("Test Artist", response.getArtistName());
        assertEquals(123L, response.getAppleCatalogId());
        assertEquals("Pop", response.getGenre());
        assertEquals(200, response.getDurationSeconds());

        verify(repository).save(any(LibraryItem.class));
    }

    @Test
    void updateItem_shouldUpdateRatingAndNotes() {
        Long itemId = 1L;
        LibraryItem existing = new LibraryItem();
        existing.setId(itemId);
        existing.setUserId(userId);
        existing.setTitle("Song");
        existing.setArtistName("Artist");

        LibraryUpdateRequest updateRequest = new LibraryUpdateRequest();
        updateRequest.setUserRating(4);
        updateRequest.setUserNotes("Great song!");

        when(repository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.of(existing));
        when(repository.save(any(LibraryItem.class))).thenAnswer(i -> i.getArgument(0));

        LibraryItemResponse response = service.updateItem(userId, itemId, updateRequest);

        assertEquals(4, response.getUserRating());
        assertEquals("Great song!", response.getUserNotes());
        verify(repository).findByIdAndUserId(itemId, userId);
        verify(repository).save(existing);
    }

    @Test
    void updateItem_shouldThrowWhenItemNotFound() {
        Long itemId = 99L;
        when(repository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateItem(userId, itemId, new LibraryUpdateRequest()));
    }

    @Test
    void updateItem_shouldThrowWhenItemBelongsToDifferentUser() {
        Long itemId = 1L;
        LibraryItem item = new LibraryItem();
        item.setId(itemId);
        item.setUserId(999L);

        when(repository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateItem(userId, itemId, new LibraryUpdateRequest()));
    }

    @Test
    void deleteItem_shouldRemoveOwnedItem() {
        Long itemId = 1L;
        LibraryItem item = new LibraryItem();
        item.setId(itemId);
        item.setUserId(userId);

        when(repository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.of(item));

        service.deleteItem(userId, itemId);

        verify(repository).delete(item);
    }

    @Test
    void deleteItem_shouldThrowWhenItemNotFound() {
        Long itemId = 99L;
        when(repository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteItem(userId, itemId));
    }

    @Test
    void deleteItem_shouldThrowWhenItemBelongsToDifferentUser() {
        Long itemId = 1L;
        when(repository.findByIdAndUserId(itemId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteItem(userId, itemId));
    }

    @Test
    void getUserLibrary_shouldReturnPagedResults() {
        LibraryItem item = new LibraryItem();
        item.setId(1L);
        item.setUserId(userId);
        item.setTitle("Song One");
        item.setArtistName("Artist One");

        Page<LibraryItem> page = new PageImpl<>(List.of(item));

        when(repository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        PagedResponse<LibraryItemResponse> result =
                service.getUserLibrary(userId, 0, 20, null, null);

        assertEquals(1, result.getContent().size());
        assertEquals("Song One", result.getContent().get(0).getTitle());
        assertEquals(0, result.getPage());
    }

    @Test
    void getUserLibrary_withFilters_shouldCallFilteredQuery() {
        when(repository.findByUserIdWithFilters(
                eq(userId), eq("Pop"), eq("2024"), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.getUserLibrary(userId, 0, 20, "Pop", "2024");

        verify(repository).findByUserIdWithFilters(
                eq(userId), eq("Pop"), eq("2024"), any(Pageable.class));
    }
}
