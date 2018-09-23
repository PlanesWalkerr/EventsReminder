package com.makhovyk.mykhailo.reminder.model;

import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;

import com.makhovyk.mykhailo.reminder.R;

import java.text.DateFormatSymbols;
import java.util.Locale;

public class Separator implements ListItem {

    String month;

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
