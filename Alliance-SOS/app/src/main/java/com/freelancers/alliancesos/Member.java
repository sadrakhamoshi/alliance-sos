package com.freelancers.alliancesos;

public class Member {
    private String id, userName;
    private boolean notDisturb;
    private boolean canChangeGroupImage;

    public Member() {
    }

    public Member(String id, String usernameForThisGroup) {
        this.id = id;
        this.userName = usernameForThisGroup;
        notDisturb = false;
        canChangeGroupImage = false;
    }

    public Member(String id, String userNameForThisGroup, boolean ischeck) {
        this.id = id;
        this.userName = userNameForThisGroup;
        this.notDisturb = ischeck;
        canChangeGroupImage = false;
    }

    public boolean isCanChangeGroupImage() {
        return canChangeGroupImage;
    }

    public void setCanChangeGroupImage(boolean canChangeGroupImage) {
        this.canChangeGroupImage = canChangeGroupImage;
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
