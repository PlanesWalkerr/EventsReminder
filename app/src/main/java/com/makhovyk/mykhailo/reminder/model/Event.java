package com.makhovyk.mykhailo.reminder.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Event implements Serializable, ListItem {

    private String type;
    private long date;
    private String eventName;
    private String personName;
    private String phone;
    private boolean isYearUnknown;
    private long timestamp;

    public Event() {
        this.timestamp = new Date().getTime();
    }


    public Event(String type, long date, String eventName, String personName, String phone, boolean isYearKnown) {
        this.type = type;
        this.date = date;
        this.eventName = eventName;
        this.personName = personName;
        this.phone = phone;
        this.isYearUnknown = isYearKnown;
        this.timestamp = new Date().getTime();
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isYearUnknown() {
        return isYearUnknown;
    }

    public void setYearUnknown(boolean yearUnknown) {
        isYearUnknown = yearUnknown;
    }

    public int getYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.YEAR);
    }

    public int getMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public int getDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp() {
        this.timestamp = new Date().getTime();
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = sdf.format(date);
        return "Event{" +
                ", type='" + type + '\'' +
                ", date='" + formattedDate + '\'' +
                ", eventName='" + eventName + '\'' +
                ", personName='" + personName + '\'' +
                ", phone='" + phone + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (date != event.date) return false;
        if (isYearUnknown != event.isYearUnknown) return false;
        if (!type.equals(event.type)) return false;
        return personName.equals(event.personName);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + personName.hashCode();
        result = 31 * result + (isYearUnknown ? 1 : 0);
        return result;
    }

    @Override
    public boolean isSeparator() {
        return false;
    }
}