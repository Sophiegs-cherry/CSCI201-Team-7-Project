package com.moodtunes.model;

import jakarta.persistence.*;

@Entity
@Table(name = "playlist_tracks")
public class PlaylistTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_id")
    private int trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @Column(name = "track_name", nullable = false, length = 200)
    private String trackName;

    @Column(name = "artist_name", nullable = false, length = 200)
    private String artistName;

    @Column(name = "youtube_music_url", nullable = false, length = 500)
    private String youtubeMusicUrl;

    @Column(name = "track_order", nullable = false)
    private int trackOrder;

    public int getTrackId() { return trackId; }
    public void setTrackId(int trackId) { this.trackId = trackId; }
    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getYoutubeMusicUrl() { return youtubeMusicUrl; }
    public void setYoutubeMusicUrl(String youtubeMusicUrl) { this.youtubeMusicUrl = youtubeMusicUrl; }
    public int getTrackOrder() { return trackOrder; }
    public void setTrackOrder(int trackOrder) { this.trackOrder = trackOrder; }
}
