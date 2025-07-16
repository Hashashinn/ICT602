package com.example.projectict;

public class Profile { //    DATABASE PURPOSE
    public String userId;
    public String name;
    public String studentId;
    public String email;
    public String imageUrl;

    public Profile() { //Default Constructor
    }

    public Profile(String userId, String name, String studentId, String email, String imageUrl) {
        this.userId = userId;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.imageUrl = imageUrl;
    }

}
