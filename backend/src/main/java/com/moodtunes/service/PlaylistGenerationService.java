package com.moodtunes.service;

import com.moodtunes.service.FlaskClientService.TrackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orchestrates playlist generation:
 *   1. Calls Flask via FlaskClientService.
 *   2. Returns the generated playlist preview without persisting it.
 *
 * Owner: Sid Goyal
 */
@Service
public class PlaylistGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistGenerationService.class);

    @Autowired
    private FlaskClientService flaskClientService;

    /**
     * Result returned to the controller, which serializes it as the HTTP response.
     */
    public static class GenerationResult {
        private final String title;
        private final String mood;
        private final String musicPreferences;
        private final String context;
        private final LocalDateTime createdAt;
        private final List<TrackDto> tracks;

        public GenerationResult(String title,
                                String mood,
                                String musicPreferences,
                                String context,
                                LocalDateTime createdAt,
                                List<TrackDto> tracks) {
            this.title = title;
            this.mood = mood;
            this.musicPreferences = musicPreferences;
            this.context = context;
            this.createdAt = createdAt;
            this.tracks = tracks;
        }

        public String getTitle() { return title; }
        public String getMood() { return mood; }
        public String getMusicPreferences() { return musicPreferences; }
        public String getContext() { return context; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public List<TrackDto> getTracks() { return tracks; }
    }

    public GenerationResult generate(String username,
                                     String moodText,
                                     String musicPreferences,
                                     String context) {
        List<TrackDto> tracks = flaskClientService.generatePlaylist(moodText, musicPreferences, context);
        logger.info("Flask returned {} tracks for user '{}', mood='{}'",
                tracks.size(), username, moodText);

        // If Flask returned zero validated tracks, treat as upstream failure rather than
        // saving an empty playlist. This bubbles up as 503 from the controller.
        if (tracks.isEmpty()) {
            throw new FlaskClientService.FlaskUnavailableException(
                    "Flask returned no validated tracks");
        }

        String title = buildTitle(moodText);
        return new GenerationResult(title, moodText, musicPreferences, context, LocalDateTime.now(), tracks);
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
