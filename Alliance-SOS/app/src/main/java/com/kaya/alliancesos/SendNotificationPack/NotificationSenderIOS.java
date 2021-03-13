package com.kaya.alliancesos.SendNotificationPack;


import com.google.gson.annotations.SerializedName;

class Notification {
    public String title;
    public String body;

    public Notification(String body, String title) {
        this.title = title;
        this.body = body;
    }
}

public class NotificationSenderIOS extends NotificationSender {

    @SerializedName("notification")
    public Notification notification;

    public NotificationSenderIOS(DataToSend dataToSend, String to) {
        super(dataToSend, to);
        this.notification = new Notification("Body", "Title");
    }

    public NotificationSenderIOS() {
    }
}
