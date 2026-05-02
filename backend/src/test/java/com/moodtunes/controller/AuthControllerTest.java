package com.moodtunes.controller;

import com.moodtunes.dto.AuthResponse;
import com.moodtunes.dto.LoginRequest;
import com.moodtunes.dto.MessageResponse;
import com.moodtunes.dto.RegisterRequest;
import com.moodtunes.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController Unit Tests
 * Covers Testing Plan sections 1.1-1.6
 * 
 * Tests 1.7-1.8 (Protected Routes) are in SecurityConfigTest.java
 */
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    /**
     * Test 1.1 — Successful Registration
     * Type: Black box, unit test
     * Input: POST /api/auth/register with valid user data
     * Expected: HTTP 200, AuthResponse with token and username
     */
    @Test
    public void testSuccessfulRegistration() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@email.com");
        request.setPassword("Password123");

        AuthResponse response = new AuthResponse();
        response.setToken("eyJhbGciOiJIUzI1NiJ9.test-jwt-token");
        response.setUsername("testuser");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    /**
     * Test 1.2 — Registration with Duplicate Username
     * Type: Black box, unit test
     * Input: POST /api/auth/register with existing username
     * Expected: HTTP 409 Conflict, MessageResponse "Username already taken"
     */
    @Test
    public void testRegistrationDuplicateUsername() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@email.com");
        request.setPassword("Password123");

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }

    /**
     * Test 1.3 — Registration with Duplicate Email
     * Type: Black box, unit test
     * Input: POST /api/auth/register with existing email
     * Expected: HTTP 409 Conflict, MessageResponse "Email already in use"
     */
    @Test
    public void testRegistrationDuplicateEmail() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@email.com");
        request.setPassword("Password123");

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    /**
     * Test 1.4 — Successful Login
     * Type: Black box, unit test
     * Input: POST /api/auth/login with valid credentials
     * Expected: HTTP 200, AuthResponse with valid JWT token
     */
    @Test
    public void testSuccessfulLogin() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Password123");

        AuthResponse response = new AuthResponse();
        response.setToken("eyJhbGciOiJIUzI1NiJ9.valid-jwt-token");
        response.setUsername("testuser");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    /**
     * Test 1.5 — Login with Wrong Password
     * Type: Black box, unit test
     * Input: POST /api/auth/login with incorrect password
     * Expected: HTTP 401 Unauthorized
     */
    @Test
    public void testLoginWrongPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("WrongPassword123");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    /**
     * Test 1.6 — Login with Nonexistent User
     * Type: Black box, unit test
     * Input: POST /api/auth/login with non-existent username
     * Expected: HTTP 401 Unauthorized
     */
    @Test
    public void testLoginNonexistentUser() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistentuser");
        request.setPassword("Password123");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    /**
     * Bonus Test — Test Endpoint
     * Verify the test endpoint is accessible
     */
    @Test
    public void testAuthTestEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Auth API is working!"));
    }
}