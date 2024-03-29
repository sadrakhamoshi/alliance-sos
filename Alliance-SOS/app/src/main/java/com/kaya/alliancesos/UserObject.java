package com.kaya.alliancesos;

import java.util.TimeZone;

public class UserObject {
    private String id;
    private String userName;
    private String email;
    private String password;
    private String token;
    private String language;
    private String timeZone;
    private String image;
    private boolean notDisturb;
    private boolean ringEnable;

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public <Type> void updateObject(String field, Type newVal) {
        switch (field) {
            case "email":
                this.setEmail((String) newVal);
                break;
            case "timeZone":
                this.setTimeZone((String) newVal);
                break;
            case "password":
                this.setPassword((String) newVal);
                break;
            case "language":
                this.setLanguage((String) newVal);
                break;
            case "userName":
                this.setUserName((String) newVal);
                break;
            case "ringEnable":
                this.setRingEnable((Boolean) newVal);
        }
    }

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
        image = "";
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
