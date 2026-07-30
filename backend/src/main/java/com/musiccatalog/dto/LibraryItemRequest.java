package com.musiccatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LibraryItemRequest {

    @NotNull
    private Long appleCatalogId;

    @NotBlank
    private String title;

    @NotBlank
    private String artistName;

    private String genre;
    private String releaseDate;
    private Integer durationSeconds;
    private String artworkUrl;

    public Long getAppleCatalogId() { return appleCatalogId; }
    public void setAppleCatalogId(Long appleCatalogId) { this.appleCatalogId = appleCatalogId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getArtworkUrl() { return artworkUrl; }
    public void setArtworkUrl(String artworkUrl) { this.artworkUrl = artworkUrl; }
}
