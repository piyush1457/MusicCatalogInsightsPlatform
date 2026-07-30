package com.musiccatalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ITunesResponse {

    @JsonProperty("resultCount")
    private int resultCount;

    @JsonProperty("results")
    private List<ITunesResult> results;

    public int getResultCount() { return resultCount; }
    public void setResultCount(int resultCount) { this.resultCount = resultCount; }
    public List<ITunesResult> getResults() { return results; }
    public void setResults(List<ITunesResult> results) { this.results = results; }

    public static class ITunesResult {
        @JsonProperty("trackId")
        private Long trackId;

        @JsonProperty("trackName")
        private String trackName;

        @JsonProperty("artistName")
        private String artistName;

        @JsonProperty("primaryGenreName")
        private String primaryGenreName;

        @JsonProperty("releaseDate")
        private String releaseDate;

        @JsonProperty("trackTimeMillis")
        private Long trackTimeMillis;

        @JsonProperty("artworkUrl100")
        private String artworkUrl100;

        public Long getTrackId() { return trackId; }
        public void setTrackId(Long trackId) { this.trackId = trackId; }
        public String getTrackName() { return trackName; }
        public void setTrackName(String trackName) { this.trackName = trackName; }
        public String getArtistName() { return artistName; }
        public void setArtistName(String artistName) { this.artistName = artistName; }
        public String getPrimaryGenreName() { return primaryGenreName; }
        public void setPrimaryGenreName(String primaryGenreName) { this.primaryGenreName = primaryGenreName; }
        public String getReleaseDate() { return releaseDate; }
        public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
        public Long getTrackTimeMillis() { return trackTimeMillis; }
        public void setTrackTimeMillis(Long trackTimeMillis) { this.trackTimeMillis = trackTimeMillis; }
        public String getArtworkUrl100() { return artworkUrl100; }
        public void setArtworkUrl100(String artworkUrl100) { this.artworkUrl100 = artworkUrl100; }
    }
}
