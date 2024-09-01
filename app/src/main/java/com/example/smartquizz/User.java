package com.example.smartquizz;

public class User {
    public String name, username, email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String username, String email) {
        this.name = name;
        this.username = username;
        this.email = email;
    }
}
