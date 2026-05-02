package com.moodtunes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.moodtunes.service.FlaskClientService.TrackDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes a JSON snapshot of every generated playlist to the local filesystem.
 *
 * Path: ./playlists/playlist-{yyyyMMdd-HHmmss}.json (relative to the working dir
 * the Spring Boot app is launched from). Directory is auto-created and gitignored.
 *
 * The file is a debugging / audit artifact — not served to users.
 *
 * Owner: Sid Goyal
 */
@Service
public class JsonFileSaveService {

    private static final Logger logger = LoggerFactory.getLogger(JsonFileSaveService.class);

    private static final DateTimeFormatter TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static final Path PLAYLISTS_DIR = Paths.get("playlists");

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Write the playlist to disk. Failure is logged but does NOT throw —
     * losing a debug file should never fail a real user request.
     *
     * @return the path written, or null on failure
     */
    public Path savePlaylist(Integer userId,
                             String mood,
                             String musicPreferences,
                             String context,
                             String playlistTitle,
                             List<TrackDto> tracks) {
        try {
            if (!Files.exists(PLAYLISTS_DIR)) {
                Files.createDirectories(PLAYLISTS_DIR);
            }

            String timestamp = LocalDateTime.now().format(TS_FORMAT);
            Path file = PLAYLISTS_DIR.resolve("playlist-" + timestamp + ".json");

            // LinkedHashMap so the JSON keys come out in a sensible order.
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("generatedAt", LocalDateTime.now().toString());
            payload.put("userId", userId);
            payload.put("mood", mood);
            payload.put("musicPreferences", musicPreferences);
            payload.put("context", context);
            payload.put("playlistTitle", playlistTitle);
            payload.put("trackCount", tracks == null ? 0 : tracks.size());
            payload.put("tracks", tracks);

            objectMapper.writeValue(file.toFile(), payload);
            logger.info("Saved playlist JSON to {}", file.toAbsolutePath());
            return file;

        } catch (IOException ex) {
            logger.error("Failed to save playlist JSON: {}", ex.getMessage(), ex);
            return null;
        }
    }
}
