package com.ilyarudyak.android.portfel.analytics;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.ilyarudyak.android.portfel.R;

/**
 * Created by ilyarudyak on 10/26/15.
 */
public class PortfelApplication extends Application {

    public Tracker mTracker;

    public void startTracking() {
        if (mTracker == null) {
            GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
            mTracker = ga.newTracker(R.xml.track_app);
            ga.enableAutoActivityReports(this);
        }
    }

    public Tracker getTracker() {
        // make sure the tracker exists
        startTracking();
        return mTracker;
    }
}
