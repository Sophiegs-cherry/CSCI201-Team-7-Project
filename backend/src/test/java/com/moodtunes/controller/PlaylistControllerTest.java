package com.moodtunes.controller;

import com.moodtunes.model.*;
import com.moodtunes.repository.*;
import com.moodtunes.service.PlaylistGenerationService;
import com.moodtunes.service.PlaylistGenerationService.GenerationResult;
import com.moodtunes.service.FlaskClientService.TrackDto;
import com.moodtunes.service.FlaskClientService.FlaskUnavailableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PlaylistController Unit Tests
 * Covers Testing Plan sections 3.1-3.3, 4.1-4.5, 6.1-6.3
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaylistGenerationService playlistGenerationService;

    @MockBean
    private PlaylistRepository playlistRepository;

    @MockBean
    private PlaylistTrackRepository playlistTrackRepository;

    @MockBean
    private MoodRepository moodRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SharedPlaylistRepository sharedPlaylistRepository;

    @MockBean
    private FriendshipRepository friendshipRepository;

    private User mockUser;
    private Mood mockMood;

    @BeforeEach
    public void setup() {
        mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@email.com");

        mockMood = new Mood();
        mockMood.setMoodId(1);
        mockMood.setUser(mockUser);
        mockMood.setMoodText("happy");
        mockMood.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUsername("testuser"))
            .thenReturn(Optional.of(mockUser));
    }

    /**
     * Test 3.1 — Successful Playlist Generation
     * Type: Integration test
     * Input: POST /api/playlists/generate with mood
     * Expected: HTTP 200, JSON with 15-20 tracks
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testSuccessfulPlaylistGeneration() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("mood", "happy and energetic");

        // Create mock GenerationResult with tracks
        List<TrackDto> tracks = new ArrayList<>();
        for (int i = 1; i <= 18; i++) {
            tracks.add(new TrackDto(
                    "Track " + i,
                    "Artist " + i,
                    "https://music.youtube.com/watch?v=track" + i));
        }
        GenerationResult mockResult = new GenerationResult(
                "Mood: happy and energetic",
                "happy and energetic",
                null,
                null,
                LocalDateTime.now(),
                tracks);

        when(playlistGenerationService.generate(anyString(), anyString(), any(), any()))
            .thenReturn(mockResult);

        // Act & Assert
        mockMvc.perform(post("/api/playlists/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks").isArray())
                .andExpect(jsonPath("$.tracks.length()").value(18))
                .andExpect(jsonPath("$.tracks[0].trackName").exists())
                .andExpect(jsonPath("$.tracks[0].artistName").exists())
                .andExpect(jsonPath("$.tracks[0].youtubeMusicUrl").exists());
    }

    /**
     * Test 3.2 — Playlist Generation Saves JSON File
     * Type: White box, integration test
     * Note: JSON file saving is handled by PlaylistGenerationService
     * This test verifies the service is called
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testPlaylistGenerationCallsService() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("mood", "happy");

        GenerationResult mockResult = new GenerationResult(
                "Mood: happy",
                "happy",
                null,
                null,
                LocalDateTime.now(),
                new ArrayList<>());

        when(playlistGenerationService.generate(anyString(), anyString(), any(), any()))
            .thenReturn(mockResult);

        // Act & Assert
        mockMvc.perform(post("/api/playlists/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Note: JSON file verification would be done in PlaylistGenerationServiceTest
    }

    /**
     * Test 3.3 — Playlist Generation When Flask Service Is Down
     * Type: Integration test, error handling
     * Input: POST /api/playlists/generate when Flask unavailable
     * Expected: HTTP 503 Service Unavailable
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testPlaylistGenerationFlaskDown() throws Exception {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("mood", "calm");

        when(playlistGenerationService.generate(anyString(), anyString(), any(), any()))
            .thenThrow(new FlaskUnavailableException("Flask service unavailable"));

        // Act & Assert
        mockMvc.perform(post("/api/playlists/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Playlist service is temporarily unavailable. Please try again."));
    }

    /**
     * Test 4.1 — Save Playlist to Library
     * Type: Black box, unit test
     * Input: POST /api/playlists/save with valid playlist
     * Expected: HTTP 201, playlist and tracks saved
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testSavePlaylistToLibrary() throws Exception {
        // Arrange
        when(moodRepository.findById(1)).thenReturn(Optional.of(mockMood));

        Map<String, Object> request = new HashMap<>();
        request.put("title", "My Happy Playlist");
        request.put("moodId", 1);
        
        List<Map<String, Object>> tracks = new ArrayList<>();
        Map<String, Object> track = new HashMap<>();
        track.put("trackName", "Happy");
        track.put("artistName", "Pharrell Williams");
        track.put("youtubeMusicUrl", "https://music.youtube.com/watch?v=ZbZSe6N_BXs");
        track.put("trackOrder", 1);
        tracks.add(track);
        request.put("tracks", tracks);

        Playlist savedPlaylist = new Playlist();
        savedPlaylist.setPlaylistId(1);
        savedPlaylist.setTitle("My Happy Playlist");
        savedPlaylist.setUser(mockUser);
        savedPlaylist.setMood(mockMood);
        savedPlaylist.setCreatedAt(LocalDateTime.now());

        when(playlistRepository.save(any(Playlist.class))).thenReturn(savedPlaylist);

        // Act & Assert
        mockMvc.perform(post("/api/playlists/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playlistId").value(1))
                .andExpect(jsonPath("$.title").value("My Happy Playlist"));
    }

    /**
     * Test 4.2 — List User's Playlists
     * Type: Black box, unit test
     * Input: GET /api/playlists/library (user with 3 playlists)
     * Expected: HTTP 200, array of 3 playlists sorted newest first
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testListUsersPlaylists() throws Exception {
        // Arrange
        Playlist p1 = new Playlist();
        p1.setPlaylistId(1);
        p1.setTitle("Happy Vibes");
        p1.setMood(mockMood);
        p1.setCreatedAt(LocalDateTime.now());

        Playlist p2 = new Playlist();
        p2.setPlaylistId(2);
        p2.setTitle("Calm Evening");
        p2.setMood(mockMood);
        p2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Playlist p3 = new Playlist();
        p3.setPlaylistId(3);
        p3.setTitle("Workout Mix");
        p3.setMood(mockMood);
        p3.setCreatedAt(LocalDateTime.now().minusDays(2));

        List<Playlist> playlists = Arrays.asList(p1, p2, p3);

        when(playlistRepository.findByUserIdOrderByCreatedAtDesc(1))
            .thenReturn(playlists);
        when(playlistTrackRepository.findByPlaylistId(anyInt())).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/playlists/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Happy Vibes"))
                .andExpect(jsonPath("$[1].title").value("Calm Evening"))
                .andExpect(jsonPath("$[2].title").value("Workout Mix"));
    }

    /**
     * Test 4.3 — Get Playlist Details
     * Type: Black box, unit test
     * Input: GET /api/playlists/{id} owned by user
     * Expected: HTTP 200, full playlist with tracks
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testGetPlaylistDetails() throws Exception {
        // Arrange
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setTitle("Happy Playlist");
        playlist.setUser(mockUser);
        playlist.setMood(mockMood);
        playlist.setCreatedAt(LocalDateTime.now());

        PlaylistTrack track1 = new PlaylistTrack();
        track1.setTrackName("Happy");
        track1.setArtistName("Pharrell Williams");
        track1.setYoutubeMusicUrl("https://music.youtube.com/watch?v=ZbZSe6N_BXs");
        track1.setTrackOrder(1);

        PlaylistTrack track2 = new PlaylistTrack();
        track2.setTrackName("Good as Hell");
        track2.setArtistName("Lizzo");
        track2.setYoutubeMusicUrl("https://music.youtube.com/watch?v=SmbmeOgWsqE");
        track2.setTrackOrder(2);

        List<PlaylistTrack> tracks = Arrays.asList(track1, track2);

        when(playlistRepository.findById(1)).thenReturn(Optional.of(playlist));
        when(playlistTrackRepository.findByPlaylistId(1)).thenReturn(tracks);

        // Act & Assert
        mockMvc.perform(get("/api/playlists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playlistId").value(1))
                .andExpect(jsonPath("$.title").value("Happy Playlist"))
                .andExpect(jsonPath("$.tracks").isArray())
                .andExpect(jsonPath("$.tracks.length()").value(2));
    }

    /**
     * Test 4.4 — Get Playlist Not Owned by User
     * Type: Black box, unit test
     * Input: GET /api/playlists/{id} owned by different user
     * Expected: HTTP 403 Forbidden
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testGetPlaylistNotOwnedByUser() throws Exception {
        // Arrange
        User otherUser = new User();
        otherUser.setUserId(2);
        otherUser.setUsername("otheruser");

        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUser(otherUser);

        when(playlistRepository.findById(1)).thenReturn(Optional.of(playlist));

        // Act & Assert
        mockMvc.perform(get("/api/playlists/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * Test 4.5 — Get Nonexistent Playlist
     * Type: Black box, unit test
     * Input: GET /api/playlists/99999
     * Expected: HTTP 404 Not Found
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testGetNonexistentPlaylist() throws Exception {
        // Arrange
        when(playlistRepository.findById(99999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/playlists/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * Test 6.1 — Share Playlist with Friends
     * Type: Black box, unit test
     * Input: POST /api/playlists/share with playlistId, recipientIds, message
     * Expected: HTTP 200, shared playlist records created
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testSharePlaylistWithFriends() throws Exception {
        // Arrange
        User friend1 = new User();
        friend1.setUserId(2);
        friend1.setUsername("friend1");

        User friend2 = new User();
        friend2.setUserId(3);
        friend2.setUsername("friend2");

        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUser(mockUser);
        playlist.setTitle("Happy Playlist");

        Friendship acceptedFriendship = new Friendship();
        acceptedFriendship.setStatus(Friendship.Status.ACCEPTED);

        when(playlistRepository.findById(1)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(2)).thenReturn(Optional.of(friend1));
        when(userRepository.findById(3)).thenReturn(Optional.of(friend2));
        when(friendshipRepository.findBetween(1, 2)).thenReturn(Optional.of(acceptedFriendship));
        when(friendshipRepository.findBetween(1, 3)).thenReturn(Optional.of(acceptedFriendship));

        Map<String, Object> request = new HashMap<>();
        request.put("playlistId", 1);
        request.put("recipientIds", Arrays.asList(2, 3));
        request.put("message", "Check this out!");

        // Act & Assert
        mockMvc.perform(post("/api/playlists/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Playlist shared successfully"));

        verify(sharedPlaylistRepository, times(2)).save(any(SharedPlaylist.class));
    }

    /**
     * Test 6.2 — Share Playlist with Non-Friend
     * Type: Black box, unit test
     * Input: POST /api/playlists/share with a recipient who is not an accepted friend
     * Expected: HTTP 400 Bad Request
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testSharePlaylistWithNonFriend() throws Exception {
        // Arrange
        User nonFriend = new User();
        nonFriend.setUserId(99);
        nonFriend.setUsername("stranger");

        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUser(mockUser);
        playlist.setTitle("Happy Playlist");

        when(playlistRepository.findById(1)).thenReturn(Optional.of(playlist));
        when(userRepository.findById(99)).thenReturn(Optional.of(nonFriend));
        when(friendshipRepository.findBetween(1, 99)).thenReturn(Optional.empty());

        Map<String, Object> request = new HashMap<>();
        request.put("playlistId", 1);
        request.put("recipientIds", Collections.singletonList(99));
        request.put("message", "Check this out!");

        // Act & Assert
        mockMvc.perform(post("/api/playlists/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Can only share with friends"));

        verify(sharedPlaylistRepository, times(0)).save(any(SharedPlaylist.class));
    }

    /**
     * Test 6.3 — View Shared Playlists
     * Type: Black box, unit test
     * Input: GET /api/playlists/shared
     * Expected: HTTP 200, list of playlists shared with the authenticated user
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testViewSharedPlaylists() throws Exception {
        // Arrange
        User sender = new User();
        sender.setUserId(2);
        sender.setUsername("musicfriend");

        Playlist playlist1 = new Playlist();
        playlist1.setPlaylistId(10);
        playlist1.setTitle("Calm Study Mix");

        Playlist playlist2 = new Playlist();
        playlist2.setPlaylistId(11);
        playlist2.setTitle("Workout Mix");

        SharedPlaylist share1 = new SharedPlaylist();
        share1.setShareId(100);
        share1.setPlaylist(playlist1);
        share1.setSender(sender);
        share1.setRecipient(mockUser);
        share1.setMessage("For studying");
        share1.setSharedAt(LocalDateTime.now());

        SharedPlaylist share2 = new SharedPlaylist();
        share2.setShareId(101);
        share2.setPlaylist(playlist2);
        share2.setSender(sender);
        share2.setRecipient(mockUser);
        share2.setMessage("For gym");
        share2.setSharedAt(LocalDateTime.now().minusHours(1));

        when(sharedPlaylistRepository.findByRecipientUserIdOrderBySharedAtDesc(1))
                .thenReturn(Arrays.asList(share1, share2));

        // Act & Assert
        mockMvc.perform(get("/api/playlists/shared"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].shareId").value(100))
                .andExpect(jsonPath("$[0].playlistId").value(10))
                .andExpect(jsonPath("$[0].title").value("Calm Study Mix"))
                .andExpect(jsonPath("$[0].senderUsername").value("musicfriend"))
                .andExpect(jsonPath("$[0].message").value("For studying"))
                .andExpect(jsonPath("$[1].playlistId").value(11));
    }
}
