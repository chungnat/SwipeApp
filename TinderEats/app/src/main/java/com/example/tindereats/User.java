package com.example.tindereats;

import android.net.Uri;

import java.util.List;

public class User {
    private String name;
    private String email;
    private String username;
    private String icon;
    private String registrationToken;

    public User(String name, String email, String username, String icon, String registrationToken) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.icon = icon;
        this.registrationToken = registrationToken;
    }

    public User() {}

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getIcon() {
        return icon;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setIcon(String uri) {
        icon = uri;
    }
}
