package com.example.alliancesos.SendNotificationPack;

import com.example.alliancesos.Utils.MessageType;

public class DataToSend<TypeOfNot> {
    private String makeBy, groupName;
    private String groupId;
    private String eventId;
    private int type;

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

    //for sos
    public DataToSend(String makeBy, String groupName, String groupId) {
        this.groupName = groupName;
        this.makeBy = makeBy;
        this.groupId = groupId;
        type = MessageType.SOS_TYPE;
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
