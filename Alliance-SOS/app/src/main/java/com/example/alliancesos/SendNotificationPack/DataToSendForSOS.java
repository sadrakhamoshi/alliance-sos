package com.example.alliancesos.SendNotificationPack;

import com.example.alliancesos.Utils.MessageType;

public class DataToSendForSOS {
    private String makeBy, groupName;
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

    public void setType(int type) {
        this.type = type;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public DataToSendForSOS(String makeBy, String groupName) {
        this.groupName = groupName;
        this.makeBy = makeBy;
        type = MessageType.SOS_TYPE;
    }

    public DataToSendForSOS() {
    }
}
