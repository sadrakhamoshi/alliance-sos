package com.example.alliancesos;

public class Member {
    private String id, userName;
    boolean notDisturb;

    public Member() {
    }

    public Member(String id, String usernameForThisGroup) {
        this.id = id;
        this.userName = usernameForThisGroup;
        notDisturb = false;
    }

    public Member(String id, String userNameForThisGroup, boolean ischeck) {
        this.id = id;
        this.userName = userNameForThisGroup;
        this.notDisturb = ischeck;
    }

    public void setNotDisturb(boolean notDisturb) {
        this.notDisturb = notDisturb;
    }

    public boolean isNotDisturb() {
        return notDisturb;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
