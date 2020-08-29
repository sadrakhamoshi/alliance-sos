package com.example.alliancesos;

public class UserObject {
    private String id;
    private String userName;
    private String email;
    private String password;
    private String token;

    public UserObject(String id, String user, String mail, String pass, String tok) {
        this.id = id;
        password = pass;
        email = mail;
        userName = user;
        token = tok;
    }

    public UserObject() {
    }

    ;

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
