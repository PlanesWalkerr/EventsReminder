package com.makhovyk.mykhailo.reminder.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.utils.Constants;
import com.makhovyk.mykhailo.reminder.utils.Utils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class AlarmHelper {

    Context context;
    SharedPreferences preferences;
    AlarmManager alarmManager;

    public AlarmHelper(Context context) {
        this.context = context.getApplicationContext();
        alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setAlarm(Event event) {

        long notifyAt = preferences.getLong(Constants.NOTIFICATION_TIME, 0);

        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(notifyAt);
        if (Utils.notifyThisYear(event.getDate())) {
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        } else {
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        }

        calendar.set(Calendar.MONTH, event.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, event.getDay());

        Intent notifyIntent = new Intent(context, AlarmReceiver.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifyIntent.putExtra(Constants.EVENT, (Serializable) event);

        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (context.getApplicationContext(), (int) event.getTimestamp(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }

    public void deleteAlarm(long timestamp) {

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) timestamp, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

    }

    public void updateAlarm(Event event) {
        deleteAlarm(event.getTimestamp());
        setAlarm(event);
    }

    public void updateAllAlarms() {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        List<Event> events = dbHelper.getEvents();
        for (Event e : events) {
            deleteAlarm(e.getTimestamp());
            setAlarm(e);
        }
    }

    public void setAllAlarms() {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        List<Event> events = dbHelper.getEvents();
        for (Event e : events) {
            setAlarm(e);
        }
    }

}
