package com.makhovyk.eventsreminder.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.makhovyk.eventsreminder.activities.EventDetailsActivity;
import com.makhovyk.eventsreminder.R;
import com.makhovyk.eventsreminder.model.Event;
import com.makhovyk.eventsreminder.utils.Constants;

import java.util.Calendar;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "com.makhovyk.eventsreminder";

    NotificationChannel channel;
    private NotificationManager manager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getBundleExtra(Constants.BUNDLE);

        if (bundle != null) {
            Event event = (Event) bundle.getSerializable(Constants.EVENT);
            long additionalAlarmShift = intent.getLongExtra(Constants.ADDITIONAL_ALARM_SHIFT, 0);
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            createNotificationChannel(context);
            Intent notifyIntent = new Intent(context, EventDetailsActivity.class);
            notifyIntent.putExtra(Constants.EVENT, event);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) event.getTimestamp(),
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentTitle(event.getType() + ": " + event.getPersonName());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.getDate());
            int year = Calendar.getInstance().get(Calendar.YEAR) - event.getYear();

            String contentText = additionalAlarmShift != 0
                    ? (context.getString(R.string.detail_days_left) + ": " + additionalAlarmShift)
                    : context.getString(R.string.today_is) + year;
            builder.setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setVibrate(new long[]{1000, 1000})
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setChannelId(CHANNEL_ID);
            }

            Notification notificationCompat = builder.build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
            managerCompat.notify((int) event.getTimestamp(), notificationCompat);
            if (additionalAlarmShift == 0) {
                new AlarmHelper(context).setupAlarms(event);
            }
        }

    }

    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

}
