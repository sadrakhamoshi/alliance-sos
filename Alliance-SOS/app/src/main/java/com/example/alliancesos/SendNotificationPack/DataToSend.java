package com.example.alliancesos.SendNotificationPack;

import com.example.alliancesos.Utils.MessageType;

public class DataToSend<TypeOfNot> {
    private String makeBy, groupName;
    private TypeOfNot content;
    private int type;

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

    public TypeOfNot getContent() {
        return content;
    }

    public void setContent(TypeOfNot content) {
        this.content = content;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public DataToSend(String makeBy, String groupName) {
        this.groupName = groupName;
        this.makeBy = makeBy;
        type = MessageType.SOS_TYPE;
    }

    public DataToSend(String makeBy, String groupName, TypeOfNot data) {
        this.content = data;
        this.makeBy = makeBy;
        this.groupName = groupName;
        type = MessageType.NOTIFICATION_TYPE;
    }

    public DataToSend() {
    }
}
