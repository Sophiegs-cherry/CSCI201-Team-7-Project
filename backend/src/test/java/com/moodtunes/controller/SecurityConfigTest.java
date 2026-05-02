package com.moodtunes.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SecurityConfig Unit Tests
 * Covers Testing Plan sections 1.7-1.8
 * 
 * Tests protected routes with and without authentication
 */
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test 1.7 — Access Protected Route Without Token
     * Type: Black box, unit test
     * Input: GET /api/playlists/library with no Authorization header
     * Expected: HTTP 401 Unauthorized
     */
    @Test
    public void testAccessProtectedRouteWithoutToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/playlists/library"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test 1.8 — Access Protected Route With Valid Token
     * Type: Black box, unit test
     * Input: GET /api/playlists/library with valid JWT (mocked user)
     * Expected: HTTP 200, returns user's playlists
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testAccessProtectedRouteWithValidToken() throws Exception {
        // Act & Assert
        // Note: This will return 200 even if playlist list is empty
        // The important thing is that authentication succeeds
        mockMvc.perform(get("/api/playlists/library"))
                .andExpect(status().isOk());
    }

    /**
     * Additional Test — Multiple Protected Routes Without Auth
     * Verify all protected endpoints require authentication
     */
    @Test
    public void testMultipleProtectedRoutesWithoutAuth() throws Exception {
        // Test mood logging
        mockMvc.perform(post("/api/moods/log"))
                .andExpect(status().isUnauthorized());

        // Test mood history
        mockMvc.perform(get("/api/moods/history"))
                .andExpect(status().isUnauthorized());

        // Test playlist generation
        mockMvc.perform(post("/api/playlists/generate"))
                .andExpect(status().isUnauthorized());

        // Test playlist save
        mockMvc.perform(post("/api/playlists/save"))
                .andExpect(status().isUnauthorized());

        // Test friends list
        mockMvc.perform(get("/api/friends"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Additional Test — Public Routes Are Accessible
     * Verify public endpoints don't require authentication
     */
    @Test
    public void testPublicRoutesAccessible() throws Exception {
        // Auth endpoints should be public
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Auth API is working!"));

        // Login and register should be accessible without auth
        // (Though they may return 400 for missing body, not 401)
    }

    /**
     * Additional Test — Protected Routes With Authentication
     * Verify authenticated user can access protected endpoints
     */
    @Test
    @WithMockUser(username = "testuser")
    public void testProtectedRoutesWithAuthentication() throws Exception {
        // These should return 200 (or other non-401 status)
        // not 401 Unauthorized
        
        // Playlist library
        mockMvc.perform(get("/api/playlists/library"))
                .andExpect(status().isOk());

        // Mood history
        mockMvc.perform(get("/api/moods/history"))
                .andExpect(status().isOk());

        // Friends list
        mockMvc.perform(get("/api/friends"))
                .andExpect(status().isOk());
    }
}