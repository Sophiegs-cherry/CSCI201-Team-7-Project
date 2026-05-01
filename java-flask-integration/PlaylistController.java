package com.moodtunes.controller;

import com.moodtunes.service.FlaskClientService.FlaskUnavailableException;
import com.moodtunes.service.PlaylistGenerationService;
import com.moodtunes.service.PlaylistGenerationService.GenerationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

/**
 * Playlist endpoints.
 *
 * ─────────────────────────────────────────────────────────────────────────
 *  CO-OWNED FILE — see RESPONSIBILITIES.md
 *  • Sid Goyal:    POST /api/playlists/generate            (this file, below)
 *  • Wenwei Fu:    POST /api/playlists/save
 *                  GET  /api/playlists/library
 *                  GET  /api/playlists/{id}
 *                  POST /api/playlists/share
 *                  GET  /api/playlists/shared
 *
 *  Wenwei: add your endpoints below the marked section. Don't change the
 *  /generate method or the class-level annotations without pinging Sid.
 * ─────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "http://localhost:3000")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @Autowired
    private PlaylistGenerationService playlistGenerationService;

    // ============================================================
    // === SID — /generate ========================================
    // ============================================================

    /**
     * Request body for POST /api/playlists/generate.
     * Mood is required; the other two fields are optional.
     */
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

    /**
     * POST /api/playlists/generate
     *
     * Authenticated endpoint. Pulls the username from the SecurityContext
     * (set by Sophie's JwtAuthenticationFilter), calls Flask, persists, and
     * returns the generated playlist.
     *
     * Status codes:
     *   200 — playlist generated and saved
     *   400 — mood missing or Flask rejected the input (HttpClientErrorException)
     *   401 — handled upstream by SecurityConfig
     *   503 — Flask unreachable / timed out / 5xx (FlaskUnavailableException)
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody GenerateRequest body) {
        // Manual validation — keeps the error shape consistent with the rest of the API
        // (Sophie's AuthController returns MessageResponse-like JSON for errors).
        if (body == null || body.getMood() == null || body.getMood().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(error("mood is required"));
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            // Should be unreachable — SecurityConfig requires auth on this path.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(error("Not authenticated"));
        }
        String username = auth.getName();

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
                    .body(error("Playlist service is temporarily unavailable. Please try again."));

        } catch (HttpClientErrorException ex) {
            // Flask returned a 4xx (e.g. Daniel's 400 for empty mood, though we already guard above).
            logger.warn("Flask rejected request for user '{}': {} {}",
                    username, ex.getStatusCode(), ex.getResponseBodyAsString());
            return ResponseEntity.badRequest()
                    .body(error("Invalid request: " + ex.getStatusCode().value()));

        } catch (Exception ex) {
            logger.error("Unexpected error generating playlist for user '{}'", username, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error("Internal error generating playlist"));
        }
    }

    private Map<String, String> error(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("error", message);
        return m;
    }

    // ============================================================
    // === WENWEI — add your endpoints below ======================
    // ============================================================
    //  POST /save, GET /library, GET /{id}, POST /share, GET /shared
    // ============================================================
}
