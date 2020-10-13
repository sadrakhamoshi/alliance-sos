package com.example.alliancesos.Payment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentObject {
    public boolean enable;
    public String month;
    public String chargeDate;

    public PaymentObject(boolean enable, String month) {
        this.enable = enable;
        this.month = month;
        convertToString(new Date());
    }

    public void convertToString(Date date) {
        String pattern = "MM/dd/yyyy hh:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        this.chargeDate = format.format(date);
    }

    public boolean expired() {
        int m = Integer.parseInt(this.month);
        long time = m * 30 * 24 * 60 * 60 * 1000;
        Date dateNow = new Date(new Date().getTime() - time);
        String pattern = "MM/dd/yyyy hh:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date date1 = null;
        try {
            date1 = format.parse(pattern);

        } catch (Exception e) {
        }
        if (dateNow.after(date1)) {
            return true;
        }
        return false;
    }
}
