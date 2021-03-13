package com.kaya.alliancesos.SendNotificationPack;

import com.google.gson.annotations.SerializedName;

public class NotificationSender {
    @SerializedName("to")
    private String to;

    @SerializedName("content_available")
    private boolean content_available;

    @SerializedName("mutable_content")
    private boolean mutable_content;
    @SerializedName("data")
    private DataToSend data;

    @SerializedName("priority")
    public String priority;

//    @SerializedName("notification")
//    public Notification notification;

    public NotificationSender(DataToSend data, String to) {
        this.data = data;
        this.to = to;
//        this.notification = new Notification("body", "title");
        this.priority = "high";
        this.mutable_content = true;
        this.content_available = true;
    }

    public NotificationSender() {
    }
}
