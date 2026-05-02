package com.moodtunes.service;

import com.moodtunes.config.FlaskConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Talks to the Flask AI microservice.
 *
 * Flask contract (from flask-service/app.py, owned by Daniel):
 *   POST {baseUrl}/generate-playlist
 *   Body: {"mood": "...", "musicPreferences": "...", "context": "..."}
 *   Returns: 200 with [{"trackName","artistName","youtubeMusicUrl"}, ...]
 *            400 if mood is empty
 *            500 on Gemini / parse failure
 *
 * Owner: Sid Goyal
 */
@Service
public class FlaskClientService {

    private static final Logger logger = LoggerFactory.getLogger(FlaskClientService.class);

    @Autowired
    private FlaskConfig flaskConfig;

    @Autowired
    @Qualifier("flaskRestTemplate")
    private RestTemplate restTemplate;

    /**
     * Simple DTO for tracks returned by Flask.
     * Public static so PlaylistGenerationService and the controller can use it.
     */
    public static class TrackDto {
        private String trackName;
        private String artistName;
        private String youtubeMusicUrl;

        public TrackDto() {}

        public TrackDto(String trackName, String artistName, String youtubeMusicUrl) {
            this.trackName = trackName;
            this.artistName = artistName;
            this.youtubeMusicUrl = youtubeMusicUrl;
        }

        public String getTrackName() { return trackName; }
        public void setTrackName(String trackName) { this.trackName = trackName; }

        public String getArtistName() { return artistName; }
        public void setArtistName(String artistName) { this.artistName = artistName; }

        public String getYoutubeMusicUrl() { return youtubeMusicUrl; }
        public void setYoutubeMusicUrl(String youtubeMusicUrl) { this.youtubeMusicUrl = youtubeMusicUrl; }
    }

    /**
     * Thrown when Flask is unreachable, times out, or returns a 5xx error.
     * The controller maps this to HTTP 503.
     */
    public static class FlaskUnavailableException extends RuntimeException {
        public FlaskUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
        public FlaskUnavailableException(String message) {
            super(message);
        }
    }

    /**
     * Call Flask to generate a playlist for the given mood inputs.
     *
     * @param mood              required, user's mood text
     * @param musicPreferences  optional, may be null/empty
     * @param context           optional, may be null/empty
     * @return list of validated tracks (may be empty if no songs validated)
     * @throws FlaskUnavailableException on connection / timeout / 5xx
     */
    @SuppressWarnings("unchecked")
    public List<TrackDto> generatePlaylist(String mood, String musicPreferences, String context) {
        String url = flaskConfig.getBaseUrl() + "/generate-playlist";

        Map<String, String> body = new HashMap<>();
        body.put("mood", mood == null ? "" : mood);
        body.put("musicPreferences", musicPreferences == null ? "" : musicPreferences);
        body.put("context", context == null ? "" : context);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Flask returns a flat JSON array, so deserialize into List<Map<...>> then map manually.
            ResponseEntity<List> response = restTemplate.postForEntity(url, request, List.class);

            List<Map<String, Object>> raw = response.getBody();
            if (raw == null) {
                logger.warn("Flask returned null body for mood='{}'", mood);
                return new ArrayList<>();
            }

            List<TrackDto> tracks = new ArrayList<>();
            for (Map<String, Object> item : raw) {
                String trackName = stringOrNull(item.get("trackName"));
                String artistName = stringOrNull(item.get("artistName"));
                String youtubeMusicUrl = stringOrNull(item.get("youtubeMusicUrl"));

                // Skip malformed entries rather than failing the whole request.
                if (trackName == null || artistName == null || youtubeMusicUrl == null) {
                    continue;
                }
                tracks.add(new TrackDto(trackName, artistName, youtubeMusicUrl));
            }
            return tracks;

        } catch (ResourceAccessException ex) {
            // Connection refused, read timeout, etc.
            logger.error("Flask service unreachable at {}: {}", url, ex.getMessage());
            throw new FlaskUnavailableException("Flask service unavailable", ex);

        } catch (HttpServerErrorException ex) {
            // Flask returned 5xx (e.g. Gemini failure)
            logger.error("Flask service returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new FlaskUnavailableException(
                    "Flask service error: " + ex.getStatusCode(), ex);
        }
        // Note: 4xx (e.g. empty mood) propagates as HttpClientErrorException so the controller
        // can surface a 400. We intentionally don't catch it here.
    }

    private static String stringOrNull(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
