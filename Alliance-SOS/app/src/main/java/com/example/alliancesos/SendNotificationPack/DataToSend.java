package com.example.alliancesos.SendNotificationPack;

public class DataToSend {
    private String title, message;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataToSend(String title, String message) {
        this.message = message;
        this.title = title;
    }

    public DataToSend() {
    }
}
