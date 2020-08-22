package com.example.alliancesos;

public class Groups {
    private String groupName, id, admin;

    public Groups(String n, String i, String ad) {
        groupName = n;
        id = i;
        admin = ad;

    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
