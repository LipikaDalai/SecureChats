package com.example.securechats.Notifications;

public class OnlineCheck {

    private String token;

    public OnlineCheck() {
    }

    public OnlineCheck(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
