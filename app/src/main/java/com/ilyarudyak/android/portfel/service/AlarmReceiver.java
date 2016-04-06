package com.ilyarudyak.android.portfel.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This is broadcast receiver that starts our service on BOOT.
 * We also add this receiver and action.BOOT_COMPLETED to
 * manifest.
 *
 * We do not start service directly but rather start alarm.
 * Does our alarm fire upon starting?
 * */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Received broadcast intent: " + intent.getAction());
        MarketUpdateService.setServiceAlarm(context, true);
    }

}
