package com.kaya.alliancesos.SendNotificationPack;

public class NotificationSender {
    private String title, body;
    private String to;
    private boolean mutable_content;
    private DataToSend data;

    public NotificationSender(DataToSend data, String to) {
        this.data = data;
        this.to = to;
        this.mutable_content = true;
        this.title = "";
        this.body = "";
    }

    public NotificationSender() {
    }
}
