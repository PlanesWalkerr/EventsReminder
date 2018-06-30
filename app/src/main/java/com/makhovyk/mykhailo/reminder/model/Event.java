package com.makhovyk.mykhailo.reminder.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Event {

    private int id;
    private String type;
    private long date;
    private String eventName;
    private String personName;
    private String email;
    private String phone;
    private boolean isYearKnown;

    public Event() {
    }

    public Event(int id, String type, long date, String eventName, String personName, String email, String phone, boolean isYearKnown) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.eventName = eventName;
        this.personName = personName;
        this.email = email;
        this.phone = phone;
        this.isYearKnown = isYearKnown;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isYearKnown() {
        return isYearKnown;
    }

    public void setYearKnown(boolean yearKnown) {
        isYearKnown = yearKnown;
    }

    public int getYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.YEAR);
    }

    public int getMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.MONTH);
    }

    public int getDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = sdf.format(date);
        return "Event{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", date='" + formattedDate + '\'' +
                ", eventName='" + eventName + '\'' +
                ", personName='" + personName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
