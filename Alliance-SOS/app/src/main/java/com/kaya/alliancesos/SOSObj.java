package com.kaya.alliancesos;
import com.kaya.alliancesos.SendNotificationPack.DataToSend;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class SOSObj {
    private String groupId, groupName, makeBy, sosId;
    private int type;
    private String photoUrl;
    private String sosMessage;
    private String timeZone;
    private long timeStamp;

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public SOSObj() {
    }

    public SOSObj(String makeBy, String groupName, String groupId, Integer message_type) {
        this.groupName = groupName;
        this.makeBy = makeBy;
        this.groupId = groupId;
        this.type = message_type;
        this.timeZone = TimeZone.getDefault().getID();
    }

    public SOSObj(DataToSend obj) {
        this.groupId = obj.getGroupId();
        this.groupName = obj.getGroupName();
        this.type = obj.getType();
        this.makeBy = obj.getMakeBy();
        if (obj.getPhotoUrl() != null) {
            this.photoUrl = obj.getPhotoUrl();
        } else {
            this.sosMessage = obj.getSosMessage();
        }
        this.sosId = obj.getSosId();
        this.timeZone = TimeZone.getDefault().getID();
        this.timeStamp = Instant.now().getEpochSecond();
    }

    public String getDateFromUTC() {
        Instant instant = Instant.ofEpochSecond(this.timeStamp);
        ZonedDateTime ztd = instant.atZone(ZoneId.systemDefault());
        String format = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm").format(ztd);
        return format;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMakeBy(String makeBy) {
        this.makeBy = makeBy;
    }

    public void setSosId(String sosId) {
        this.sosId = sosId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setSosMessage(String sosMessage) {
        this.sosMessage = sosMessage;
    }

    public String getMakeBy() {
        return makeBy;
    }

    public String getSosId() {
        return sosId;
    }

    public int getType() {
        return type;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getSosMessage() {
        return sosMessage;
    }

}
