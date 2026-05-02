package com.moodtunes.service;

import com.moodtunes.config.FlaskConfig;
import com.moodtunes.service.FlaskClientService.FlaskUnavailableException;
import com.moodtunes.service.FlaskClientService.TrackDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * FlaskClientService unit tests — covers the Spring-side fallback logic
 * that PlaylistControllerTest only sees through a mocked service:
 *   - Plan 3.3: connection refused / timeout → FlaskUnavailableException → 503
 *   - 5xx from Flask → FlaskUnavailableException
 *   - 4xx from Flask propagates (controller maps to 400)
 *   - Malformed track entries are skipped, not failed
 *   - Null body returns empty list
 */
@ExtendWith(MockitoExtension.class)
class FlaskClientServiceTest {

    @Mock private RestTemplate restTemplate;
    @Mock private FlaskConfig flaskConfig;

    @InjectMocks private FlaskClientService flaskClientService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(flaskClientService, "restTemplate", restTemplate);
        when(flaskConfig.getBaseUrl()).thenReturn("http://localhost:5001");
    }

    @Test
    void generatePlaylist_returnsTracksOnSuccess() {
        List<Map<String, Object>> body = new ArrayList<>();
        body.add(track("Happy", "Pharrell", "https://music.youtube.com/watch?v=ZbZSe6N_BXs"));
        body.add(track("Calm Down", "Rema", "https://music.youtube.com/watch?v=abc"));

        when(restTemplate.postForEntity(any(String.class), any(), eq(List.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<TrackDto> tracks = flaskClientService.generatePlaylist("happy", "pop", "party");

        assertEquals(2, tracks.size());
        assertEquals("Happy", tracks.get(0).getTrackName());
        assertEquals("Pharrell", tracks.get(0).getArtistName());
    }

    @Test
    void generatePlaylist_throwsFlaskUnavailableOnConnectionRefused() {
        // Plan 3.3 — Flask :5001 is down
        when(restTemplate.postForEntity(any(String.class), any(), eq(List.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        FlaskUnavailableException ex = assertThrows(FlaskUnavailableException.class,
                () -> flaskClientService.generatePlaylist("happy", null, null));
        assertTrue(ex.getMessage().toLowerCase().contains("unavailable"));
    }

    @Test
    void generatePlaylist_throwsFlaskUnavailableOn5xx() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(List.class)))
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Gemini failed", null, null, null));

        assertThrows(FlaskUnavailableException.class,
                () -> flaskClientService.generatePlaylist("happy", null, null));
    }

    @Test
    void generatePlaylist_propagates4xxToController() {
        // 4xx (e.g. empty mood) must propagate so controller can map to HTTP 400
        when(restTemplate.postForEntity(any(String.class), any(), eq(List.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.BAD_REQUEST,
                        "mood is required", null, null, null));

        assertThrows(HttpClientErrorException.class,
                () -> flaskClientService.generatePlaylist("", null, null));
    }

    @Test
    void generatePlaylist_skipsMalformedEntries() {
        List<Map<String, Object>> body = new ArrayList<>();
        body.add(track("Happy", "Pharrell", "https://music.youtube.com/watch?v=1"));
        body.add(track(null, "Artist", "https://music.youtube.com/watch?v=2"));        // missing trackName
        body.add(track("Title", "", "https://music.youtube.com/watch?v=3"));            // empty artist
        body.add(track("Title2", "Artist2", null));                                     // null URL

        when(restTemplate.postForEntity(any(String.class), any(), eq(List.class)))
                .thenReturn(ResponseEntity.ok(body));

        List<TrackDto> tracks = flaskClientService.generatePlaylist("happy", null, null);

        assertEquals(1, tracks.size(), "Only the fully-formed track survives");
        assertEquals("Happy", tracks.get(0).getTrackName());
    }

    @Test
    void generatePlaylist_returnsEmptyListOnNullBody() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(List.class)))
                .thenReturn(ResponseEntity.ok(null));

        List<TrackDto> tracks = flaskClientService.generatePlaylist("happy", null, null);
        assertNotNull(tracks);
        assertTrue(tracks.isEmpty());
    }

    @Test
    void generatePlaylist_handlesNullOptionalFields() {
        when(restTemplate.postForEntity(any(String.class), any(), eq(List.class)))
                .thenReturn(ResponseEntity.ok(new ArrayList<>()));

        // Should not throw NPE when musicPreferences/context are null
        assertDoesNotThrow(() ->
                flaskClientService.generatePlaylist("happy", null, null));
    }

    private Map<String, Object> track(String name, String artist, String url) {
        Map<String, Object> m = new HashMap<>();
        m.put("trackName", name);
        m.put("artistName", artist);
        m.put("youtubeMusicUrl", url);
        return m;
    }
}
