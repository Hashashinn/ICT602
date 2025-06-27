package com.example.projectict;

public class StudentProfile {
    public String userId;
    public String name;
    public String studentId;
    public String email;

    public StudentProfile() {
        // Required by Firebase
    }

    public StudentProfile(String userId, String name, String studentId, String email) {
        this.userId = userId;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
    }
}
