package com.makhovyk.eventsreminder.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.DatePicker;

import java.text.DateFormatSymbols;


public class CustomDatePickerDialog extends DatePickerDialog {

    private boolean hideYear;

    public CustomDatePickerDialog(@NonNull Context context, @Nullable OnDateSetListener listener,
                                  int year, int month, int dayOfMonth, boolean hideYear) {
        super(context, listener, year, month, dayOfMonth);
        this.hideYear = hideYear;
    }

    @Override
    public void onDateChanged(@NonNull DatePicker view, int year, int month, int dayOfMonth) {
        super.onDateChanged(view, year, month, dayOfMonth);
        if (hideYear) {
            String monthString = new DateFormatSymbols().getMonths()[month];
            setTitle(monthString + " " + dayOfMonth);
        }
    }
}
