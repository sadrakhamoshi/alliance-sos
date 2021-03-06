package com.kaya.alliancesos;

public class InviteObject {
    private String inviteId, groupId, groupName, invitedBy, userId;

    public InviteObject(String inviteId, String invitedBy, String userId, String groupName, String groupId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.userId = userId;
        this.invitedBy = invitedBy;
        this.inviteId = inviteId;
    }

    public InviteObject() {

    }

    public String getGroupName() {
        return groupName;
    }

    public String getUserId() {
        return userId;
    }

    public String getInviteId() {
        return inviteId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInviteId(String inviteId) {
        this.inviteId = inviteId;
    }
}
