package com.streamvault.models;

public class ContentItem {
    private int    contentId;
    private String title;
    private String contentType;   // Movie | Series | Documentary | Podcast
    private String synopsis;
    private int    releaseYear;
    private int    durationMinutes;
    private String language;
    private String ageRating;
    private String studioName;
    private double avgRating;
    private String genres;        // comma-separated for display
    private String posterUrl;     // image path from ContentItems.poster_url

    public ContentItem() {}

    // ── Getters & Setters ──────────────────────────────────────────────────
    public int    getContentId()        { return contentId; }
    public void   setContentId(int v)   { contentId = v; }

    public String getTitle()            { return title; }
    public void   setTitle(String v)    { title = v; }

    public String getContentType()         { return contentType; }
    public void   setContentType(String v) { contentType = v; }

    public String getSynopsis()          { return synopsis; }
    public void   setSynopsis(String v)  { synopsis = v; }

    public int  getReleaseYear()       { return releaseYear; }
    public void setReleaseYear(int v)  { releaseYear = v; }

    public int  getDurationMinutes()      { return durationMinutes; }
    public void setDurationMinutes(int v) { durationMinutes = v; }

    public String getLanguage()         { return language; }
    public void   setLanguage(String v) { language = v; }

    public String getAgeRating()         { return ageRating; }
    public void   setAgeRating(String v) { ageRating = v; }

    public String getStudioName()         { return studioName; }
    public void   setStudioName(String v) { studioName = v; }

    public double getAvgRating()        { return avgRating; }
    public void   setAvgRating(double v){ avgRating = v; }

    public String getGenres()           { return genres; }
    public void   setGenres(String v)   { genres = v; }

    public String getPosterUrl()        { return posterUrl; }
    public void   setPosterUrl(String v){ posterUrl = v; }
}