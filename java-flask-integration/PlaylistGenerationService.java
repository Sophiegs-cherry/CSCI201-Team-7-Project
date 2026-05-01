package com.moodtunes.service;

import com.moodtunes.model.Mood;
import com.moodtunes.model.Playlist;
import com.moodtunes.model.PlaylistTrack;
import com.moodtunes.model.User;
import com.moodtunes.repository.MoodRepository;
import com.moodtunes.repository.PlaylistRepository;
import com.moodtunes.repository.PlaylistTrackRepository;
import com.moodtunes.repository.UserRepository;
import com.moodtunes.service.FlaskClientService.TrackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates playlist generation:
 *   1. Resolves the current user from username.
 *   2. Calls Flask via FlaskClientService.
 *   3. Persists Mood + Playlist + PlaylistTrack rows via Wenwei's repos.
 *   4. Writes the JSON snapshot via JsonFileSaveService (best-effort).
 *
 * NOTE FOR WENWEI: This file imports Mood, Playlist, PlaylistTrack and their
 * repositories. The setter calls below match the field names documented in
 * RESPONSIBILITIES.md. If you change a setter name (e.g. setMoodText →
 * setText), update this file in the same PR — ping Sid.
 *
 * Owner: Sid Goyal
 */
@Service
public class PlaylistGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistGenerationService.class);

    @Autowired
    private FlaskClientService flaskClientService;

    @Autowired
    private JsonFileSaveService jsonFileSaveService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MoodRepository moodRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistTrackRepository playlistTrackRepository;

    /**
     * Result returned to the controller, which serializes it as the HTTP response.
     */
    public static class GenerationResult {
        private final Integer playlistId;
        private final String title;
        private final String mood;
        private final List<TrackDto> tracks;

        public GenerationResult(Integer playlistId, String title, String mood, List<TrackDto> tracks) {
            this.playlistId = playlistId;
            this.title = title;
            this.mood = mood;
            this.tracks = tracks;
        }

        public Integer getPlaylistId() { return playlistId; }
        public String getTitle() { return title; }
        public String getMood() { return mood; }
        public List<TrackDto> getTracks() { return tracks; }
    }

    @Transactional
    public GenerationResult generate(String username,
                                     String moodText,
                                     String musicPreferences,
                                     String context) {
        // 1. Resolve user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB: " + username));

        // 2. Call Flask
        List<TrackDto> tracks = flaskClientService.generatePlaylist(moodText, musicPreferences, context);
        logger.info("Flask returned {} tracks for user '{}', mood='{}'",
                tracks.size(), username, moodText);

        // If Flask returned zero validated tracks, treat as upstream failure rather than
        // saving an empty playlist. This bubbles up as 503 from the controller.
        if (tracks.isEmpty()) {
            throw new FlaskClientService.FlaskUnavailableException(
                    "Flask returned no validated tracks");
        }

        // 3. Persist Mood
        Mood mood = new Mood();
        mood.setUser(user);
        mood.setMoodText(moodText);
        mood.setMusicPreferences(musicPreferences);
        mood.setContextNote(context);
        mood.setCreatedAt(LocalDateTime.now());
        mood = moodRepository.save(mood);

        // 4. Persist Playlist
        String title = buildTitle(moodText);
        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setMood(mood);
        playlist.setTitle(title);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist = playlistRepository.save(playlist);

        // 5. Persist tracks (preserve order from Flask, 1-indexed per RESPONSIBILITIES.md)
        List<PlaylistTrack> savedTracks = new ArrayList<>();
        int order = 1;
        for (TrackDto t : tracks) {
            PlaylistTrack pt = new PlaylistTrack();
            pt.setPlaylist(playlist);
            pt.setTrackName(t.getTrackName());
            pt.setArtistName(t.getArtistName());
            pt.setYoutubeMusicUrl(t.getYoutubeMusicUrl());
            pt.setTrackOrder(order++);
            savedTracks.add(pt);
        }
        if (!savedTracks.isEmpty()) {
            playlistTrackRepository.saveAll(savedTracks);
        }

        // 6. Write JSON snapshot (best-effort; failures already logged inside)
        jsonFileSaveService.savePlaylist(
                user.getUserId(), moodText, musicPreferences, context, title, tracks);

        return new GenerationResult(playlist.getPlaylistId(), title, moodText, tracks);
    }

    /**
     * Build a short, human-friendly playlist title from the mood text.
     * Keeps it under 60 chars so it fits on the LibraryPage cards.
     */
    private String buildTitle(String moodText) {
        if (moodText == null || moodText.isBlank()) {
            return "Mood Playlist";
        }
        String trimmed = moodText.trim();
        if (trimmed.length() > 50) {
            trimmed = trimmed.substring(0, 47) + "...";
        }
        return "Mood: " + trimmed;
    }
}
