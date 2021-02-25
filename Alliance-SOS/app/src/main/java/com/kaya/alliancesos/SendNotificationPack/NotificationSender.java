package com.kaya.alliancesos.SendNotificationPack;

import com.google.gson.annotations.SerializedName;

class Notification {
    public String title;
    public String body;

    public Notification(String title, String body) {
        this.title = title;
        this.body = body;
    }
}

public class NotificationSender {
    @SerializedName("to")
    private String to;

    @SerializedName("mutable_content")
    private boolean mutable_content;
    @SerializedName("data")
    private DataToSend data;

    @SerializedName("priority")
    public String priority;

    @SerializedName("notification")
    public Notification notification;

    public NotificationSender(DataToSend data, String to) {
        this.data = data;
        this.to = to;
        this.notification = new Notification("nothing", "nothing");
        this.priority = "high";
        this.mutable_content = true;
    }

    public NotificationSender() {
    }
}
