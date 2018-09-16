package com.makhovyk.mykhailo.reminder.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.utils.Constants;
import com.makhovyk.mykhailo.reminder.utils.Utils;

import java.util.Calendar;
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

    public void setAlarm(Event event, boolean postpone) {
        long notifyAt = preferences.getLong(Constants.NOTIFICATION_TIME, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(notifyAt);
        if (!postpone) {
            if (Utils.notifyThisYear(event.getDate())) {
                calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            } else {
                calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
            }
        } else {
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        }

        calendar.set(Calendar.MONTH, event.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, event.getDay());

        Intent notifyIntent = new Intent(context, AlarmReceiver.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EVENT, event);
        notifyIntent.putExtra("bundle", bundle);
//        notifyIntent.putExtra(Constants.EVENT, (Serializable) event);
//        notifyIntent.setAction(event.getTimestamp() + "");
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);



        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (context.getApplicationContext(), (int) event.getTimestamp(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        Log.d("hello", calendar.getTime().toString());

    }

    public void deleteAlarm(long timestamp) {

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) timestamp, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

    }

    public void updateAlarm(Event event) {
        deleteAlarm(event.getTimestamp());
        setAlarm(event, false);
    }

    public void updateAllAlarms() {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        List<Event> events = dbHelper.getEvents();
        for (Event e : events) {
            deleteAlarm(e.getTimestamp());
            setAlarm(e, false);
        }
    }

    public void setAllAlarms() {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        List<Event> events = dbHelper.getEvents();
        for (Event e : events) {
            setAlarm(e, false);
        }
    }

}
