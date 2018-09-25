package com.makhovyk.eventsreminder.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.makhovyk.eventsreminder.R;
import com.makhovyk.eventsreminder.database.SQLiteDBHelper;
import com.makhovyk.eventsreminder.model.Event;
import com.makhovyk.eventsreminder.utils.Constants;
import com.makhovyk.eventsreminder.utils.Utils;

import java.util.Calendar;
import java.util.List;

public class AlarmHelper {

    private Context context;
    private SharedPreferences preferences;
    private AlarmManager alarmManager;
    private long defaultTime;
    private final long MILLIS_IN_DAY = 86400000;
    private final long REQUEST_CODE_SHIFT = 1234;

    public AlarmHelper(Context context) {
        this.context = context.getApplicationContext();
        alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Calendar defaultCalendar = Calendar.getInstance();
        defaultCalendar.set(Calendar.HOUR_OF_DAY, 9);
        defaultCalendar.set(Calendar.MINUTE, 0);
        defaultTime = defaultCalendar.getTimeInMillis();
    }

    public void setupAlarms(Event event) {

        setDefaultAlarm(event);
        long additionalAlarm = Long.parseLong(preferences
                .getString(context.getString(R.string.key_additional_notification), "0"));
        if (additionalAlarm != 0) {
            setAdditionalAlarm(event, additionalAlarm);
        }

    }


    public void deleteAlarms(long timestamp) {

        deleteDefaultAlarm(timestamp);
        long additionalAlarmShift = Long.parseLong(preferences
                .getString(context.getString(R.string.key_additional_notification), "0"));
        deleteAdditionalAlarm(timestamp);
    }

    private void deleteDefaultAlarm(long timestamp) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) timestamp, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private void deleteAdditionalAlarm(long timestamp) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        timestamp += REQUEST_CODE_SHIFT;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) timestamp, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }


    public void updateAlarm(Event event) {
        deleteAlarms(event.getTimestamp());
        setupAlarms(event);
    }

    public void updateAllAlarms() {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        List<Event> events = dbHelper.getEvents();
        for (Event e : events) {
            deleteAlarms(e.getTimestamp());
            setupAlarms(e);
        }
    }

    public void setAllAlarms() {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        List<Event> events = dbHelper.getEvents();
        for (Event e : events) {
            setupAlarms(e);
        }
    }

    private void setDefaultAlarm(Event event) {
        long notificationTime = setupNotificationTime(event, 0);
        Intent notifyIntent = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EVENT, event);
        notifyIntent.putExtra(Constants.BUNDLE, bundle);
        notifyIntent.putExtra(Constants.ADDITIONAL_ALARM_SHIFT, 0);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(notificationTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (context.getApplicationContext(), (int) event.getTimestamp(), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        setAlarmDependingOnApi(notificationTime, pendingIntent);
    }

    private void setAdditionalAlarm(Event event, long shift) {
        long notificationTime = setupNotificationTime(event, shift);
        Intent notifyIntent = new Intent(context, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EVENT, event);
        notifyIntent.putExtra(Constants.BUNDLE, bundle);
        notifyIntent.putExtra(Constants.ADDITIONAL_ALARM_SHIFT, shift);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(notificationTime);

        int requestCode = (int) (event.getTimestamp() + REQUEST_CODE_SHIFT);

        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (context.getApplicationContext(), requestCode, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        setAlarmDependingOnApi(notificationTime, pendingIntent);
    }

    private long setupNotificationTime(Event event, long additionalAlarmOffset) {
        long notifyAt = preferences.getLong(Constants.NOTIFICATION_TIME, defaultTime);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(notifyAt);
        calendar.set(Calendar.MONTH, event.getMonth() - 1);
        calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        calendar.set(Calendar.DAY_OF_MONTH, event.getDay());
        calendar.set(Calendar.SECOND, 0);
        calendar.setTimeInMillis(calendar.getTimeInMillis() - (additionalAlarmOffset * MILLIS_IN_DAY));

        if (Utils.notifyThisYear(calendar.getTimeInMillis())) {
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        } else {
            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) + 1);
        }

        return calendar.getTimeInMillis();
    }

    private void setAlarmDependingOnApi(long notificationTime, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }

    }

}
