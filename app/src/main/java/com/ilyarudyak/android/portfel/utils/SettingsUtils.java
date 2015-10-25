package com.ilyarudyak.android.portfel.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ilyarudyak.android.portfel.R;

/**
 * Created by ilyarudyak on 10/25/15.
 */
public class SettingsUtils {

    private static final String TAG = SettingsUtils.class.getSimpleName();

    public static boolean isNotificationsSet(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.settings_notifications_key), true);
    }

    public static long getAlarmInterval(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int interval = Integer.parseInt(sp.getString(context.getString(R.string.settings_refreshing_key), "12"));
        switch (interval) {
            case 1:
                return AlarmManager.INTERVAL_HOUR;
            case 12:
                return AlarmManager.INTERVAL_HALF_DAY;
            case 24:
                return AlarmManager.INTERVAL_DAY;
            default:
                throw new IllegalArgumentException("unknown argument");
        }
    }
}
