package com.moodtunes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "moods")
public class Mood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mood_id")
    private int moodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mood_category", nullable = false)
    private String moodText;

    @Column(name = "genre_preferences")
    private String musicPreferences;

    @Column(name = "context_note", columnDefinition = "TEXT")
    private String contextNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "mood", fetch = FetchType.LAZY)
    private Playlist playlist;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public int getMoodId() { return moodId; }
    public void setMoodId(int moodId) { this.moodId = moodId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMoodText() { return moodText; }
    public void setMoodText(String moodText) { this.moodText = moodText; }
    public String getMusicPreferences() { return musicPreferences; }
    public void setMusicPreferences(String musicPreferences) { this.musicPreferences = musicPreferences; }
    public String getContextNote() { return contextNote; }
    public void setContextNote(String contextNote) { this.contextNote = contextNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
}
