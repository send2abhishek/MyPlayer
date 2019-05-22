package com.attra.myplayer.Utils;

public class Songs {

    private String title;
    private String albumName;
    private String path;
    private String artistName;
    private String Composer;
    private long duration;
    private int year;

    public Songs(String title, String albumName, String path, String artistName, String composer,
                 long duration, int year) {
        this.title = title;
        this.albumName = albumName;
        this.path = path;
        this.artistName = artistName;
        Composer = composer;
        this.duration = duration;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getPath() {
        return path;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getComposer() {
        return Composer;
    }

    public long getDuration() {
        return duration;
    }

    public int getYear() {
        return year;
    }
}
