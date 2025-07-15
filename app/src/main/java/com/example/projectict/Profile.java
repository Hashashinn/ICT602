package com.example.projectict;

public class Profile {
    public String userId;
    public String name;
    public String studentId;
    public String email;

    public Profile() {
    }

    public Profile(String userId, String name, String studentId, String email) {
        this.userId = userId;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
    }
}
