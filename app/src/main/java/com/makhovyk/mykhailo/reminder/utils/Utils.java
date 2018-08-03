package com.makhovyk.mykhailo.reminder.utils;

import com.makhovyk.mykhailo.reminder.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Utils {

    public static boolean isYearUnknown(String dateString) {
        String[] data = dateString.split("-");
        return data[0].equals("");
    }

    public static Long getDateFromString(String dateString) {
        String[] data = dateString.split("-");
        Calendar calendar = Calendar.getInstance();

        if (data[0].equals("")) {
            calendar.set(Calendar.MONTH, Integer.parseInt(data[2]));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[3]));
        } else {
            calendar.set(Calendar.YEAR, Integer.parseInt(data[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(data[1]));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[2]));
        }

        return calendar.getTimeInMillis();
    }

    public static boolean isEventAlreadyInDB(ArrayList<Event> list, Event event) {
        boolean flag = false;
        for (Event e : list) {
            if (e.equals(event)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public static boolean notifyThisYear(long date) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date);
        if (calendar.get(Calendar.MONTH) >= currentCalendar.get(Calendar.MONTH)) {
            if (calendar.get(Calendar.DAY_OF_MONTH) >= currentCalendar.get(Calendar.DAY_OF_MONTH)) {
                return true;
            }
        }
        return false;
    }
}
