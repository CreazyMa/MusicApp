package cn.itcast.musicapp.bean;

/**
 * Created by CreazyMa on 2017/3/1.
 */

public class Mp3Info {
    private long id; // 歌曲ID 3
    private String title; // 歌曲名称 0

    private String album; // 专辑 7
    private long albumId;//专辑ID 6
    private String displayName; //显示名称 4
    private String artist; // 歌手名称 2
    private long duration; // 歌曲时长 1
    private long size; // 歌曲大小 8
    private String url; // 歌曲路径 5
    private String lrcTitle; // 歌词名称
    private String lrcSize; // 歌词大小
    private String picUrl;//网络歌曲图片
    private int isMusic;
    private boolean loveMusic;
    private String lrcLink;

    public boolean isLoveMusic() {
        return loveMusic;
    }

    public void setLoveMusic(boolean loveMusic) {
        this.loveMusic = loveMusic;
    }

    public String getLrcLink() {
        return lrcLink;
    }

    public void setLrcLink(String lrcLink) {
        this.lrcLink = lrcLink;
    }

    public int getIsMusic() {
        return isMusic;
    }

    public void setIsMusic(int isMusic) {
        this.isMusic = isMusic;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Mp3Info(String album, String url, String title, long size, long albumId, String artist, String displayName, long duration, long id, String lrcSize, String lrcTitle) {
        this.album = album;
        this.url = url;
        this.title = title;
        this.size = size;
        this.albumId = albumId;
        this.artist = artist;
        this.displayName = displayName;
        this.duration = duration;
        this.id = id;
        this.lrcSize = lrcSize;
        this.lrcTitle = lrcTitle;
    }

    public Mp3Info() {
        super();
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLrcSize(String lrcSize) {
        this.lrcSize = lrcSize;
    }

    public void setLrcTitle(String lrcTitle) {
        this.lrcTitle = lrcTitle;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlbum() {
        return album;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getArtist() {
        return artist;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getDuration() {
        return duration;
    }

    public long getId() {
        return id;
    }

    public String getLrcSize() {
        return lrcSize;
    }

    public String getLrcTitle() {
        return lrcTitle;
    }

    public long getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Mp3Info{" +
                "album='" + album + '\'' +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", albumId=" + albumId +
                ", displayName='" + displayName + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", lrcTitle='" + lrcTitle + '\'' +
                ", lrcSize='" + lrcSize + '\'' +
                '}';
    }

    public Runnable getBigPicUri() {
        return null;
    }
}
