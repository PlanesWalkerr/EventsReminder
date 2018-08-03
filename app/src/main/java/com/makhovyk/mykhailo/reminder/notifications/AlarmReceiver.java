package com.makhovyk.mykhailo.reminder.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.makhovyk.mykhailo.reminder.ListActivity;
import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.utils.Constants;

import java.util.Calendar;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "com.makhovyk.mykhailo.reminder";

    private NotificationManager manager;


    @Override
    public void onReceive(Context context, Intent intent) {
        Event event = (Event) intent.getSerializableExtra(Constants.EVENT);
//        String personName = intent.getStringExtra(Constants.PERSON_NAME);
//        String eventType = intent.getStringExtra(Constants.EVENT_TYPE);
//        long date = intent.getLongExtra(Constants.EVENT_DATE, 0);
//        long timestamp = intent.getLongExtra(Constants.EVENT_TIMESTAMP, 0);
        Log.v("TAG", event.toString());
        createNotificationChannel(context);
        Intent notifyIntent = new Intent(context, ListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) event.getTimestamp(),
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSound(alarmSound);
        builder.setContentTitle(event.getType() + ": " + event.getPersonName());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(event.getDate());
        builder.setContentText(calendar.getTime().toString());
        builder.setSmallIcon(R.drawable.notification);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        Notification notificationCompat = builder.build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify((int) event.getTimestamp(), notificationCompat);
        new AlarmHelper(context).setAlarm(event);
    }

    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
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
