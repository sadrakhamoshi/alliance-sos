package com.example.alliancesos;

public class Member {
    private String id, userName;
    boolean notDisturb;

    public Member() {
    }

    public Member(String id, String thisGroupName) {
        this.id = id;
        this.userName = thisGroupName;
        notDisturb = false;
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
