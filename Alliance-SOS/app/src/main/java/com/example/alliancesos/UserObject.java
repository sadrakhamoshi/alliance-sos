package com.example.alliancesos;

import com.firebase.ui.auth.data.model.User;

import java.util.TimeZone;

public class UserObject {
    private String id;
    private String userName;
    private String email;
    private String password;
    private String token;
    private String language;
    private String timeZone;
    private boolean notDisturb;
    private boolean ringEnable;

    public UserObject(String id, String user, String mail, String pass, String tok) {
        this.id = id;
        password = pass;
        email = mail;
        userName = user;
        token = tok;
        timeZone = TimeZone.getDefault().getID();
        notDisturb = false;
        ringEnable = true;
        language = "English";
    }

    public UserObject() {
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setRingEnable(boolean ringEnable) {
        this.ringEnable = ringEnable;
    }

    public boolean isRingEnable() {
        return ringEnable;
    }

    public boolean isNotDisturb() {
        return notDisturb;
    }

    public void setNotDisturb(boolean notDisturb) {
        this.notDisturb = notDisturb;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
