package com.fsmoking.app.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Reschedules WorkManager jobs after device reboot,
 * since WorkManager periodic work survives reboots by default on API 23+
 * but explicit rescheduling ensures reliability.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NotificationScheduler.scheduleAll(context);
        }
    }
}