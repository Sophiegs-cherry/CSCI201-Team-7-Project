package com.moodtunes.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moodtunes.service.FlaskClientService.TrackDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonFileSaveService unit tests — Plan 3.2 (white-box) at the Spring side.
 * Flask side is covered separately in flask-service/tests/test_app.py.
 *
 * Writes to ./playlists/ relative to the test working dir, then cleans up
 * the file it created.
 */
class JsonFileSaveServiceTest {

    private static final Path PLAYLISTS_DIR = Paths.get("playlists");
    private final JsonFileSaveService service = new JsonFileSaveService();
    private Path createdFile;

    @AfterEach
    void cleanup() throws IOException {
        if (createdFile != null && Files.exists(createdFile)) {
            Files.delete(createdFile);
        }
    }

    @Test
    void savePlaylist_writesFileToPlaylistsDirectory() throws IOException {
        List<TrackDto> tracks = Arrays.asList(
                new TrackDto("Happy", "Pharrell", "https://music.youtube.com/watch?v=1"),
                new TrackDto("Calm", "Rema", "https://music.youtube.com/watch?v=2")
        );

        Path file = service.savePlaylist(1, "happy", "pop", "party",
                "Mood: happy", tracks);
        createdFile = file;

        assertNotNull(file, "Service should return the path");
        assertTrue(Files.exists(file), "File must exist on disk");
        assertTrue(file.getFileName().toString().startsWith("playlist-"),
                "Filename must start with 'playlist-'");
        assertTrue(file.getFileName().toString().endsWith(".json"),
                "Filename must end with .json");
        assertTrue(file.getParent().endsWith("playlists"),
                "File must be under playlists/ dir");
    }

    @Test
    void savePlaylist_writesValidJsonWithExpectedFields() throws IOException {
        List<TrackDto> tracks = new ArrayList<>();
        tracks.add(new TrackDto("Happy", "Pharrell", "https://music.youtube.com/watch?v=1"));

        Path file = service.savePlaylist(42, "happy", "pop", "morning",
                "My Title", tracks);
        createdFile = file;

        JsonNode root = new ObjectMapper().readTree(file.toFile());
        assertEquals(42, root.get("userId").asInt());
        assertEquals("happy", root.get("mood").asText());
        assertEquals("pop", root.get("musicPreferences").asText());
        assertEquals("morning", root.get("context").asText());
        assertEquals("My Title", root.get("playlistTitle").asText());
        assertEquals(1, root.get("trackCount").asInt());
        assertNotNull(root.get("generatedAt"));
        assertTrue(root.get("tracks").isArray());
        assertEquals(1, root.get("tracks").size());
        assertEquals("Happy", root.get("tracks").get(0).get("trackName").asText());
    }

    @Test
    void savePlaylist_autoCreatesPlaylistsDirectory() throws IOException {
        // If the dir was deleted, savePlaylist should recreate it
        if (Files.exists(PLAYLISTS_DIR) && Files.list(PLAYLISTS_DIR).findAny().isEmpty()) {
            Files.delete(PLAYLISTS_DIR);
        }

        List<TrackDto> tracks = Arrays.asList(
                new TrackDto("X", "Y", "https://music.youtube.com/watch?v=z"));
        Path file = service.savePlaylist(1, "calm", null, null, "Title", tracks);
        createdFile = file;

        assertTrue(Files.exists(PLAYLISTS_DIR), "playlists/ dir must be auto-created");
        assertTrue(Files.exists(file));
    }

    @Test
    void savePlaylist_handlesNullTracksAsZeroCount() throws IOException {
        Path file = service.savePlaylist(1, "happy", null, null, "Title", null);
        createdFile = file;

        JsonNode root = new ObjectMapper().readTree(file.toFile());
        assertEquals(0, root.get("trackCount").asInt());
        assertTrue(root.get("tracks").isNull() || root.get("tracks").isArray());
    }

    @Test
    void savePlaylist_handlesEmptyTrackList() throws IOException {
        Path file = service.savePlaylist(1, "happy", null, null, "Title",
                new ArrayList<>());
        createdFile = file;

        JsonNode root = new ObjectMapper().readTree(file.toFile());
        assertEquals(0, root.get("trackCount").asInt());
        assertTrue(root.get("tracks").isArray());
        assertEquals(0, root.get("tracks").size());
    }
}
