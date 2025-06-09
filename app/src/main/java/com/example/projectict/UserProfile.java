package com.example.projectict;

public class UserProfile {
    public String userId;
    public String name;
    public String studentId;
    public String email;

    public UserProfile() {
        // Required by Firebase
    }

    public UserProfile(String userId, String name, String studentId, String email) {
        this.userId = userId;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
    }
}
