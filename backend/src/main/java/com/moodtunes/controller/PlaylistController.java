package com.moodtunes.controller;

import com.moodtunes.model.*;
import com.moodtunes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

/**
 * Wenwei owns: /save, /library, /{id}, /share, /shared
 * Sid owns:    /generate  (add that method below when merging sid/backend-java-flask-integration)
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

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

    // ── POST /api/playlists/save ──────────────────────────────────────────────
    // Request: { title, moodId, tracks: [{trackName, artistName, youtubeMusicUrl, trackOrder}] }
    @PostMapping("/save")
    public ResponseEntity<?> savePlaylist(@RequestBody Map<String, Object> request, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Object moodIdObj = request.get("moodId");
        if (moodIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "moodId is required"));
        }
        int moodId = ((Number) moodIdObj).intValue();
        Mood mood = moodRepository.findById(moodId)
                .orElseThrow(() -> new RuntimeException("Mood not found"));

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
                sharedPlaylistRepository.findByRecipientIdOrderBySharedAtDesc(user.getUserId());

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
        if (playlist == null || playlist.getUser().getUserId() != user.getUserId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Playlist not found or access denied"));
        }

        List<PlaylistTrack> tracks = playlistTrackRepository.findByPlaylistId(id);
        return ResponseEntity.ok(buildPlaylistResponse(playlist, tracks));
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
