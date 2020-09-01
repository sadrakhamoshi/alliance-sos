package com.example.alliancesos.SendNotificationPack;

public class NotificationSender {
    private String to;
    private DataToSendForSOS data;

    public NotificationSender(DataToSendForSOS data, String to) {
        this.data = data;
        this.to = to;
    }

    public NotificationSender() {
    }
}
