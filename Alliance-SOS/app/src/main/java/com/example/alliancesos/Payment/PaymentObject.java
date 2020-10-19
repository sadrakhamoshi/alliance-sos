package com.example.alliancesos.Payment;

import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public boolean donateExpired(int month) {
        if (TextUtils.isEmpty(this.month)) {
            return true;
        }
        int m = Integer.parseInt(this.month);
        m = m - month;
        Calendar calendar = Calendar.getInstance();
        Date dateNow = new Date();
        String pattern = "MM/dd/yyyy HH:mm";
        Date newDate = null;
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date date1 = format.parse(this.chargeDate);
            calendar.setTime(date1);
            calendar.add(Calendar.MONTH, m);
            newDate = calendar.getTime();
        } catch (Exception e) {
        }
        if (dateNow.after(newDate)) {
            return true;
        }
        return false;
    }

    public PaymentObject() {

    }

    public void convertToString(Date date) {
        String pattern = "MM/dd/yyyy HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        this.chargeDate = format.format(date);
    }

    public boolean expired() {
        if (TextUtils.isEmpty(this.month)) {
            return true;
        }
        int m = Integer.parseInt(this.month);
        Calendar calendar = Calendar.getInstance();
        Date dateNow = new Date();
        String pattern = "MM/dd/yyyy HH:mm";
        Date newDate = null;
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Date date1 = format.parse(this.chargeDate);
            calendar.setTime(date1);
            calendar.add(Calendar.MONTH, m);
            newDate = calendar.getTime();
        } catch (Exception e) {
        }
        if (dateNow.after(newDate)) {
            return true;
        }
        return false;
    }
}
