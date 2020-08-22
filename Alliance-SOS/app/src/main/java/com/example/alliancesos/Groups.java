package com.example.alliancesos;

public class Groups {
    private String groupName, id;

    public Groups(String n, String i){
        groupName =n;
        id=i;

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
