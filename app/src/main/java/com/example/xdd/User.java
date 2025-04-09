package com.example.xdd;

public class User {
    private String userId;
    private String username;
    private String fcmToken;

    public User() {}  // Necesario para Firestore

    public User(String userId, String username, String fcmToken) {
        this.userId = userId;
        this.username = username;
        this.fcmToken = fcmToken;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFcmToken() { return fcmToken; }
}