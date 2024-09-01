package com.example.login;

import androidx.annotation.Nullable;

public class HelperClass {
    private String name;
    private String usn;
    private String email;
    private String username;
    private String password;
    private String imageUrl;

    // Default constructor required for calls to DataSnapshot.getValue(HelperClass.class)
    public HelperClass() {
    }

    public HelperClass(String name, String usn, String email, String username, String password,String imageUrl) {
        this.name = name;
        this.usn = usn;
        this.email = email;
        this.username = username;
        this.password = password;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = usn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
