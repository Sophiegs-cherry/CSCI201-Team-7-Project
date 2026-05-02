package com.moodtunes.controller;

import com.moodtunes.model.*;
import com.moodtunes.repository.*;
import com.moodtunes.service.FlaskClientService.FlaskUnavailableException;
import com.moodtunes.service.PlaylistGenerationService;
import com.moodtunes.service.PlaylistGenerationService.GenerationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.util.*;

/**
 * Wenwei owns: /save, /library, /{id}, /share, /shared
 * Sid owns:    /generate  (add that method below when merging sid/backend-java-flask-integration)
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @Autowired
    private PlaylistGenerationService playlistGenerationService;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistTrackRepository playlistTrackRepository;

    @Autowired
    private MoodRepository moodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SharedPlaylistRepository sharedPlaylistRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    public static class GenerateRequest {
        private String mood;
        private String musicPreferences;
        private String context;

        public String getMood() { return mood; }
        public void setMood(String mood) { this.mood = mood; }

        public String getMusicPreferences() { return musicPreferences; }
        public void setMusicPreferences(String musicPreferences) { this.musicPreferences = musicPreferences; }

        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }

    // ── POST /api/playlists/generate ─────────────────────────────────────────
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody GenerateRequest body, Principal principal) {
        if (body == null || body.getMood() == null || body.getMood().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "mood is required"));
        }
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        String username = principal.getName();
        try {
            GenerationResult result = playlistGenerationService.generate(
                    username,
                    body.getMood().trim(),
                    body.getMusicPreferences(),
                    body.getContext()
            );
            return ResponseEntity.ok(result);
        } catch (FlaskUnavailableException ex) {
            logger.warn("Flask unavailable for user '{}': {}", username, ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Playlist service is temporarily unavailable. Please try again."));
        } catch (HttpClientErrorException ex) {
            logger.warn("Flask rejected request for user '{}': {} {}",
                    username, ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request: " + ex.getStatusCode().value()));
        } catch (Exception ex) {
            logger.error("Unexpected error generating playlist for user '{}'", username, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal error generating playlist"));
        }
    }

    // ── POST /api/playlists/save ──────────────────────────────────────────────
    // Request: { title, moodId? OR mood, musicPreferences?, context?,
    //            tracks: [{trackName, artistName, youtubeMusicUrl, trackOrder}] }
    @PostMapping("/save")
    public ResponseEntity<?> savePlaylist(@RequestBody Map<String, Object> request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Object moodIdObj = request.get("moodId");
        Mood mood;
        if (moodIdObj != null) {
            int moodId = ((Number) moodIdObj).intValue();
            mood = moodRepository.findById(moodId)
                    .orElseThrow(() -> new RuntimeException("Mood not found"));
        } else {
            String moodText = (String) request.get("mood");
            if (moodText == null || moodText.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "mood is required"));
            }
            mood = new Mood();
            mood.setUser(user);
            mood.setMoodText(moodText);
            mood.setMusicPreferences((String) request.get("musicPreferences"));
            mood.setContextNote((String) request.get("context"));
            mood.setCreatedAt(java.time.LocalDateTime.now());
            mood = moodRepository.save(mood);
        }

        String title = (String) request.get("title");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "title is required"));
        }

        Playlist playlist = new Playlist();
        playlist.setUser(user);
        playlist.setMood(mood);
        playlist.setTitle(title);
        Playlist saved = playlistRepository.save(playlist);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trackDtos = (List<Map<String, Object>>) request.get("tracks");
        List<PlaylistTrack> tracks = new ArrayList<>();
        if (trackDtos != null) {
            for (Map<String, Object> dto : trackDtos) {
                PlaylistTrack track = new PlaylistTrack();
                track.setPlaylist(saved);
                track.setTrackName((String) dto.get("trackName"));
                track.setArtistName((String) dto.get("artistName"));
                track.setYoutubeMusicUrl((String) dto.get("youtubeMusicUrl"));
                track.setTrackOrder(((Number) dto.get("trackOrder")).intValue());
                tracks.add(track);
            }
            playlistTrackRepository.saveAll(tracks);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(buildPlaylistResponse(saved, tracks));
    }

    // ── GET /api/playlists/library ────────────────────────────────────────────
    @GetMapping("/library")
    public ResponseEntity<?> getLibrary(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Playlist> playlists = playlistRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());

        List<Map<String, Object>> response = new ArrayList<>();
        for (Playlist p : playlists) {
            List<PlaylistTrack> tracks = playlistTrackRepository.findByPlaylistId(p.getPlaylistId());
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("playlistId", p.getPlaylistId());
            entry.put("title", p.getTitle());
            entry.put("createdAt", p.getCreatedAt());
            entry.put("songCount", tracks.size());
            if (p.getMood() != null) {
                entry.put("mood", p.getMood().getMoodText());
            }
            response.add(entry);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /api/playlists/shared ─────────────────────────────────────────────
    // Must be declared before /{id} so Spring MVC matches "shared" as a literal path.
    @GetMapping("/shared")
    public ResponseEntity<?> getSharedWithMe(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SharedPlaylist> sharedPlaylists =
                sharedPlaylistRepository.findByRecipientUserIdOrderBySharedAtDesc(user.getUserId());

        List<Map<String, Object>> response = new ArrayList<>();
        for (SharedPlaylist sp : sharedPlaylists) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("shareId", sp.getShareId());
            entry.put("playlistId", sp.getPlaylist().getPlaylistId());
            entry.put("title", sp.getPlaylist().getTitle());
            entry.put("senderUsername", sp.getSender().getUsername());
            entry.put("message", sp.getMessage());
            entry.put("sharedAt", sp.getSharedAt());
            response.add(entry);
        }
        return ResponseEntity.ok(response);
    }

    // ── GET /api/playlists/{id} ───────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlaylist(@PathVariable int id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Playlist playlist = playlistRepository.findById(id).orElse(null);
        if (playlist == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Playlist not found"));
        }

        boolean ownsPlaylist = Objects.equals(playlist.getUser().getUserId(), user.getUserId());
        Optional<SharedPlaylist> sharedPlaylist = ownsPlaylist
                ? Optional.empty()
                : sharedPlaylistRepository.findByPlaylistPlaylistIdAndRecipientUserId(id, user.getUserId());
        boolean sharedWithUser = sharedPlaylist.isPresent();
        if (!ownsPlaylist && !sharedWithUser) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Playlist not found or access denied"));
        }

        List<PlaylistTrack> tracks = playlistTrackRepository.findByPlaylistId(id);
        Map<String, Object> response = buildPlaylistResponse(playlist, tracks);
        sharedPlaylist.ifPresent(sp -> {
            response.put("shared", true);
            response.put("sharedMessage", sp.getMessage());
            response.put("sharedAt", sp.getSharedAt());
            response.put("sharedBy", sp.getSender().getUsername());
        });
        return ResponseEntity.ok(response);
    }

    // ── POST /api/playlists/share ─────────────────────────────────────────────
    // Request: { playlistId, recipientIds: [int], message: String (optional) }
    @PostMapping("/share")
    public ResponseEntity<?> sharePlaylist(@RequestBody Map<String, Object> request, Principal principal) {
        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Object playlistIdObj = request.get("playlistId");
        if (playlistIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "playlistId is required"));
        }
        int playlistId = ((Number) playlistIdObj).intValue();
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        if (playlist.getUser().getUserId() != sender.getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You do not own this playlist"));
        }

        @SuppressWarnings("unchecked")
        List<Integer> recipientIds = (List<Integer>) request.get("recipientIds");
        if (recipientIds == null || recipientIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "recipientIds is required"));
        }
        String message = (String) request.get("message");

        for (Object idObj : recipientIds) {
            int recipientId = ((Number) idObj).intValue();
            User recipient = userRepository.findById(recipientId).orElse(null);
            if (recipient == null) continue;

            Optional<Friendship> friendship = friendshipRepository.findBetween(sender.getUserId(), recipientId);
            if (friendship.isEmpty() || friendship.get().getStatus() != Friendship.Status.ACCEPTED) {
                return ResponseEntity.badRequest().body(Map.of("error", "Can only share with friends"));
            }

            SharedPlaylist sp = new SharedPlaylist();
            sp.setPlaylist(playlist);
            sp.setSender(sender);
            sp.setRecipient(recipient);
            sp.setMessage(message);
            sharedPlaylistRepository.save(sp);
        }

        return ResponseEntity.ok(Map.of("message", "Playlist shared successfully"));
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, Object> buildPlaylistResponse(Playlist playlist, List<PlaylistTrack> tracks) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("playlistId", playlist.getPlaylistId());
        response.put("title", playlist.getTitle());
        response.put("createdAt", playlist.getCreatedAt());

        if (playlist.getMood() != null) {
            Map<String, Object> moodMap = new LinkedHashMap<>();
            moodMap.put("moodId", playlist.getMood().getMoodId());
            moodMap.put("moodText", playlist.getMood().getMoodText());
            moodMap.put("musicPreferences", playlist.getMood().getMusicPreferences());
            moodMap.put("contextNote", playlist.getMood().getContextNote());
            response.put("mood", moodMap);
        }

        List<Map<String, Object>> trackList = new ArrayList<>();
        for (PlaylistTrack t : tracks) {
            Map<String, Object> trackMap = new LinkedHashMap<>();
            trackMap.put("trackId", t.getTrackId());
            trackMap.put("trackName", t.getTrackName());
            trackMap.put("artistName", t.getArtistName());
            trackMap.put("youtubeMusicUrl", t.getYoutubeMusicUrl());
            trackMap.put("trackOrder", t.getTrackOrder());
            trackList.add(trackMap);
        }
        response.put("tracks", trackList);
        return response;
    }
}
