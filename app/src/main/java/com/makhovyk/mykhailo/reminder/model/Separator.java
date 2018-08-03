package com.makhovyk.mykhailo.reminder.model;

import java.text.DateFormatSymbols;

public class Separator implements ListItem {

    int monthNumber;

    public Separator(int monthNumber) {
        this.monthNumber = monthNumber;
    }

    @Override
    public boolean isSeparator() {
        return true;
    }

    public String getMonth() {
        return new DateFormatSymbols().getMonths()[monthNumber - 1];
    }
}
