package com.example.alliancesos.SendNotificationPack;

import com.example.alliancesos.Utils.MessageType;

public class DataToSend {
    private String makeBy, groupName;
    private String toName, toId;
    private String groupId;
    private String eventId;
    private int type;
    private String photoUrl;
    private String sosMessage, sosId;

    public String getSosId() {
        return sosId;
    }

    public void setSosId(String sosId) {
        this.sosId = sosId;
    }

    public String getSosMessage() {
        return sosMessage;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setSosMessage(String sosMessage) {
        this.sosMessage = sosMessage;
    }

    public String getToId() {
        return toId;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setMakeBy(String makeBy) {
        this.makeBy = makeBy;
    }

    public String getMakeBy() {
        return makeBy;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getType() {
        return type;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    //for SOS or Invitation
    public DataToSend(String makeBy, String groupName, String groupId, Integer message_type) {
        this.groupName = groupName;
        this.makeBy = makeBy;
        this.groupId = groupId;
        this.type = message_type;
    }

    //for notification
    public DataToSend(String makeBy, String groupName, String groupId, String eventId) {
        this.eventId = eventId;
        this.groupId = groupId;
        this.makeBy = makeBy;
        this.groupName = groupName;
        type = MessageType.NOTIFICATION_TYPE;
    }

    public DataToSend() {
    }
}
