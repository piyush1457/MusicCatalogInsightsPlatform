package com.musiccatalog.dto;

import com.musiccatalog.entity.LibraryItem;
import java.time.Instant;

public class LibraryItemResponse {

    private Long id;
    private Long appleCatalogId;
    private String title;
    private String artistName;
    private String genre;
    private String releaseDate;
    private Integer durationSeconds;
    private String artworkUrl;
    private Integer userRating;
    private String userNotes;
    private Instant createdAt;
    private Instant updatedAt;

    public static LibraryItemResponse fromEntity(LibraryItem item) {
        LibraryItemResponse r = new LibraryItemResponse();
        r.id = item.getId();
        r.appleCatalogId = item.getAppleCatalogId();
        r.title = item.getTitle();
        r.artistName = item.getArtistName();
        r.genre = item.getGenre();
        r.releaseDate = item.getReleaseDate();
        r.durationSeconds = item.getDurationSeconds();
        r.artworkUrl = item.getArtworkUrl();
        r.userRating = item.getUserRating();
        r.userNotes = item.getUserNotes();
        r.createdAt = item.getCreatedAt();
        r.updatedAt = item.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getAppleCatalogId() { return appleCatalogId; }
    public String getTitle() { return title; }
    public String getArtistName() { return artistName; }
    public String getGenre() { return genre; }
    public String getReleaseDate() { return releaseDate; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public String getArtworkUrl() { return artworkUrl; }
    public Integer getUserRating() { return userRating; }
    public String getUserNotes() { return userNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
