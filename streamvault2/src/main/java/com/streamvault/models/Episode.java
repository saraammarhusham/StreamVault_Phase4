package com.streamvault.models;

public class Episode {
    private int    episodeId;
    private int    contentId;
    private int    seasonNumber;
    private int    episodeNumber;
    private String title;
    private int    durationMinutes;
    private String synopsis;

    public Episode() {}

    public int    getEpisodeId()         { return episodeId; }
    public void   setEpisodeId(int v)    { episodeId = v; }

    public int    getContentId()         { return contentId; }
    public void   setContentId(int v)    { contentId = v; }

    public int    getSeasonNumber()      { return seasonNumber; }
    public void   setSeasonNumber(int v) { seasonNumber = v; }

    public int    getEpisodeNumber()     { return episodeNumber; }
    public void   setEpisodeNumber(int v){ episodeNumber = v; }

    public String getTitle()             { return title; }
    public void   setTitle(String v)     { title = v; }

    public int    getDurationMinutes()      { return durationMinutes; }
    public void   setDurationMinutes(int v) { durationMinutes = v; }

    public String getSynopsis()          { return synopsis; }
    public void   setSynopsis(String v)  { synopsis = v; }
}
