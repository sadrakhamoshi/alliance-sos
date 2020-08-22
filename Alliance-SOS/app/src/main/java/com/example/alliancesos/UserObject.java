package com.example.alliancesos;

public class UserObject {
    private String userName;
    private String email;
    private String password;

    public UserObject(String user, String mail, String pass) {
        password = pass;
        email = mail;
        userName = user;
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
