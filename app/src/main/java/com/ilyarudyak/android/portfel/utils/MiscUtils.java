package com.ilyarudyak.android.portfel.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;

import com.ilyarudyak.android.portfel.R;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ilyarudyak on 10/1/15.
 */
public class MiscUtils {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static final String PATTERN_MONTH_ONLY = "MMM";
    private static final String PATTERN_TIME_ONLY = "h:mm a";

    /** format changes in stocks and indices:
     *  add + to positive changes and
     *  % to changes in percent.
     * */
    public static boolean isNonNegative(BigDecimal number) {
        return number.compareTo(new BigDecimal(0)) >= 0;
    }
    public static String formatChanges(BigDecimal change, boolean isPercent) {

        String result = change.toString();
        if (isNonNegative(change)) {
            result = "+" + result;
        }

        if (isPercent) {
            result = result + "%";
        }

        return result;
    }

    /**
     * format time elapsed like 10 minutes ago etc.
     * */
    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    /** format date for a chart on stock detail page - just month*/
    public static String formatMonthOnly(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_MONTH_ONLY);
        return sdf.format(date);

    }

    /**
     * format Date object to show only time like this 16:00
     * */
    public static String formatTimeOnly(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_TIME_ONLY);
        return sdf.format(date);

    }

    public static boolean isNetworkAvailableAndConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }
}
