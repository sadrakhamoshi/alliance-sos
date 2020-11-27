package com.freelancers.alliancesos;

public class Groups {
    private String groupName, id, admin;
    private String image;
    private UpComingEvent upComingEvent;

    public Groups(String n, String i, String ad) {
        groupName = n;
        id = i;
        admin = ad;
        image = "";
        upComingEvent = new UpComingEvent("nothing", null);
    }

    public UpComingEvent getUpComingEvent() {
        return upComingEvent;
    }

    public void setUpComingEvent(UpComingEvent upComingEvent) {
        this.upComingEvent = upComingEvent;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
