package com.freelancers.alliancesos.SendNotificationPack;

public class NotificationSender {
    private String to;
    private DataToSend data;

    public NotificationSender(DataToSend data, String to) {
        this.data = data;
        this.to = to;
    }

    public NotificationSender() {
    }
}
