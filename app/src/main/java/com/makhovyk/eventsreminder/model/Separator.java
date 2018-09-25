package com.makhovyk.eventsreminder.model;

public class Separator implements Item {

    private String month;

    public Separator(String month) {
        this.month = month;
    }

    @Override
    public boolean isSeparator() {
        return true;
    }

    public String getMonth() {
        return month;
    }
}
