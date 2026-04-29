package com.moodtunes.dto;

public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Integer userId;
    private String username;
    private String email;
    private String displayName;

    // Constructors
    public AuthResponse() {
    }

    public AuthResponse(String token, Integer userId, String username, String email, String displayName) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}