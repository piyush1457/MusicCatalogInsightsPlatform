package com.musiccatalog.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsSummary {

    private List<GenreCount> genreDistribution;
    private List<YearCount> releasesByYear;
    private List<ArtistCount> topArtists;
    private DurationHistogram durationHistogram;
    private int totalItems;

    public static class GenreCount {
        private String genre;
        private long count;
        public GenreCount(String genre, long count) { this.genre = genre; this.count = count; }
        public String getGenre() { return genre; }
        public long getCount() { return count; }
    }

    public static class YearCount {
        private String year;
        private long count;
        public YearCount(String year, long count) { this.year = year; this.count = count; }
        public String getYear() { return year; }
        public long getCount() { return count; }
    }

    public static class ArtistCount {
        private String artistName;
        private long count;
        public ArtistCount(String artistName, long count) { this.artistName = artistName; this.count = count; }
        public String getArtistName() { return artistName; }
        public long getCount() { return count; }
    }

    public static class DurationHistogram {
        private long bucket0to2;
        private long bucket2to3;
        private long bucket3to4;
        private long bucket4to5;
        private long bucket5plus;
        public DurationHistogram() {}
        public long getBucket0to2() { return bucket0to2; }
        public void setBucket0to2(long bucket0to2) { this.bucket0to2 = bucket0to2; }
        public long getBucket2to3() { return bucket2to3; }
        public void setBucket2to3(long bucket2to3) { this.bucket2to3 = bucket2to3; }
        public long getBucket3to4() { return bucket3to4; }
        public void setBucket3to4(long bucket3to4) { this.bucket3to4 = bucket3to4; }
        public long getBucket4to5() { return bucket4to5; }
        public void setBucket4to5(long bucket4to5) { this.bucket4to5 = bucket4to5; }
        public long getBucket5plus() { return bucket5plus; }
        public void setBucket5plus(long bucket5plus) { this.bucket5plus = bucket5plus; }
    }

    public List<GenreCount> getGenreDistribution() { return genreDistribution; }
    public void setGenreDistribution(List<GenreCount> genreDistribution) { this.genreDistribution = genreDistribution; }
    public List<YearCount> getReleasesByYear() { return releasesByYear; }
    public void setReleasesByYear(List<YearCount> releasesByYear) { this.releasesByYear = releasesByYear; }
    public List<ArtistCount> getTopArtists() { return topArtists; }
    public void setTopArtists(List<ArtistCount> topArtists) { this.topArtists = topArtists; }
    public DurationHistogram getDurationHistogram() { return durationHistogram; }
    public void setDurationHistogram(DurationHistogram durationHistogram) { this.durationHistogram = durationHistogram; }
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
}
