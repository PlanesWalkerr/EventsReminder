package com.makhovyk.mykhailo.reminder.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // on device boot compelete, reset the alarm

            AlarmHelper alarmHelper = new AlarmHelper(context);
            alarmHelper.setAllAlarms();

        }
    }
}
