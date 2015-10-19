package com.ilyarudyak.android.portfel.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;
import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.api.Config;
import com.ilyarudyak.android.portfel.data.PortfolioContract;
import com.ilyarudyak.android.portfel.ui.MainActivity;
import com.ilyarudyak.android.portfel.utils.DataUtils;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.DataFormatException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Fetch indices and stocks from list that we store in prefs and
 * insert them in DB (we clear DB every time before inserting data).
 * We insert indices and then stocks so we do preserve order between
 * categories but not between symbols in each category.
 * */
public class MarketUpdateService extends IntentService {

    public static final String TAG = MarketUpdateService.class.getSimpleName();
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_HALF_DAY;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int NOTIFY_ID = 1337;

    public MarketUpdateService() {
        super(TAG);

    }

    public static void setServiceAlarm(Context context) {

        Log.d(TAG, "setting alarm...");

        Intent i = newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), POLL_INTERVAL, pi);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MarketUpdateService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "starting service...");

        // fetch data only if network is available
        if (!MiscUtils.isNetworkAvailableAndConnected(this)) {
            return;
        }

        // notify about company that user choose in prefs
        notifyCompanyNews();

        Log.d(TAG, "network is available...");
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        Uri dirStockUri = PortfolioContract.StockTable.CONTENT_URI;

        // delete stocks from db before fetching
        cpo.add(ContentProviderOperation.newDelete(dirStockUri).build());

        // get indices and stocks from shared prefs; we store them in one table in DB
        String[] indexSymbols = PrefUtils.toArray(PrefUtils.getSymbols(getBaseContext(), PrefUtils.INDEX));
        String[] stockSymbols = PrefUtils.toArray(PrefUtils.getSymbols(getBaseContext(), PrefUtils.STOCK));

        // fetch new information about indices and stocks and add it to queue
        addToBatch(cpo, fetchSymbols(indexSymbols), dirStockUri);
        addToBatch(cpo, fetchSymbols(stockSymbols), dirStockUri);

        try {
            getContentResolver().applyBatch(PortfolioContract.CONTENT_AUTHORITY, cpo);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "inserting data into db DONE");
    }

    // helper methods
    private Map<String, Stock> fetchSymbols(String[] symbols) {
        Map<String, Stock> symbolsMap = null;
        try {
            symbolsMap = YahooFinance.get(symbols);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return symbolsMap;
    }
    private void addToBatch(ArrayList<ContentProviderOperation> cpo,
                            Map<String, Stock> symbolsMap, Uri uri) {
        if (symbolsMap != null) {
            Log.d(TAG, "inserting data into db...");
            for (Stock stock: symbolsMap.values()) {
                ContentValues valuesStock = DataUtils.buildContentValues(stock);
                cpo.add(ContentProviderOperation.newInsert(uri).withValues(valuesStock).build());
            }
        }
    }

    // ------------------- notifications ---------------------

    /**
     * We notify about company news.
     * When: one time per day (we store previous time of update in prefs).
     * What: the first news from feed (specific feed for this company).
     * Company: the stock symbol that a user can set in prefs.
     * onClick: DetailStockActivity for this stock is open.
     * */
    private void notifyCompanyNews() {

        Feed feed = fetchFeed();

        if (feed != null && isNotify()) {
            // we use first news item
            Notification notification = buildNotification(feed.getItems().get(0));
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIFY_ID, notification);
            updateLastNotificationTime();
        }
    }

    // helper methods
    private Feed fetchFeed() {
        Feed feed = null;
        InputStream inputStream;
        try {
            inputStream = Config.REUTERS_URL.openConnection().getInputStream();
            feed = EarlParser.parseOrThrow(inputStream, 0);
        } catch (IOException | DataFormatException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return feed;
    }
    private boolean isNotify() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lastNotificationKey = this.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);
        // check if time elapsed since last notification is more than 1 day
        return System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS;
    }
    private Notification buildNotification(Item rssItem) {

        String description = rssItem.getDescription();
        final int DESCRIPTION_SIZE = 140;
        if (description != null) {
            description = description.substring(0, DESCRIPTION_SIZE) + "...";
        }

        // build normal notification
        Notification.Builder nb = new Notification.Builder(this);
        nb.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(rssItem.getTitle())
                .setContentText(description)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_HIGH);

        Intent i = new Intent(this, MainActivity.class);
        nb.setContentIntent(PendingIntent.getActivity(this, 0, i, 0));

        // build big and reach notification
        Notification.BigTextStyle bts = new Notification.BigTextStyle(nb);
        bts.bigText(description);

        return bts.build();
    }
    private void updateLastNotificationTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lastNotificationKey = this.getString(R.string.pref_last_notification);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(lastNotificationKey, System.currentTimeMillis());
        editor.apply();
    }

}










