package com.moodtunes.service;

import com.moodtunes.service.FlaskClientService.FlaskUnavailableException;
import com.moodtunes.service.FlaskClientService.TrackDto;
import com.moodtunes.service.PlaylistGenerationService.GenerationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * PlaylistGenerationService unit tests — service-layer logic
 * (PlaylistControllerTest mocks this service entirely, so its
 *  orchestration code was previously untested).
 */
@ExtendWith(MockitoExtension.class)
class PlaylistGenerationServiceTest {

    @Mock private FlaskClientService flaskClientService;

    @InjectMocks private PlaylistGenerationService service;

    @Test
    void generate_returnsResultWithTracksFromFlask() {
        List<TrackDto> mockTracks = Arrays.asList(
                new TrackDto("Happy", "Pharrell", "https://music.youtube.com/watch?v=1"),
                new TrackDto("Calm", "Rema", "https://music.youtube.com/watch?v=2")
        );
        when(flaskClientService.generatePlaylist("happy", "pop", "party"))
                .thenReturn(mockTracks);

        GenerationResult result = service.generate("alice", "happy", "pop", "party");

        assertNotNull(result);
        assertEquals(2, result.getTracks().size());
        assertEquals("happy", result.getMood());
        assertEquals("pop", result.getMusicPreferences());
        assertEquals("party", result.getContext());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void generate_buildsTitleFromMoodText() {
        when(flaskClientService.generatePlaylist("happy and energetic", null, null))
                .thenReturn(Collections.singletonList(
                        new TrackDto("Track", "Artist", "url")));

        GenerationResult result = service.generate("alice", "happy and energetic", null, null);

        assertTrue(result.getTitle().startsWith("Mood: "),
                "Title should be prefixed with 'Mood: '");
        assertTrue(result.getTitle().contains("happy and energetic"));
    }

    @Test
    void generate_truncatesLongMoodTextInTitle() {
        String longMood = "this is a really long mood text that goes way beyond fifty characters and should be truncated";
        when(flaskClientService.generatePlaylist(longMood, null, null))
                .thenReturn(Collections.singletonList(
                        new TrackDto("Track", "Artist", "url")));

        GenerationResult result = service.generate("alice", longMood, null, null);

        assertTrue(result.getTitle().length() <= 60,
                "Title must stay under 60 chars for LibraryPage cards");
        assertTrue(result.getTitle().endsWith("..."),
                "Truncated title must end with '...'");
    }

    @Test
    void generate_throwsFlaskUnavailableWhenFlaskReturnsNoTracks() {
        // Empty list = upstream couldn't validate any songs → treat as 503
        when(flaskClientService.generatePlaylist("happy", null, null))
                .thenReturn(new ArrayList<>());

        assertThrows(FlaskUnavailableException.class,
                () -> service.generate("alice", "happy", null, null));
    }

    @Test
    void generate_propagatesFlaskUnavailableException() {
        when(flaskClientService.generatePlaylist("happy", null, null))
                .thenThrow(new FlaskUnavailableException("Connection refused"));

        assertThrows(FlaskUnavailableException.class,
                () -> service.generate("alice", "happy", null, null));
    }

    @Test
    void generate_passesOptionalFieldsThroughToFlask() {
        when(flaskClientService.generatePlaylist("calm", "lo-fi", "studying"))
                .thenReturn(Collections.singletonList(
                        new TrackDto("Track", "Artist", "url")));

        GenerationResult result = service.generate("alice", "calm", "lo-fi", "studying");

        assertEquals("lo-fi", result.getMusicPreferences());
        assertEquals("studying", result.getContext());
    }
}
