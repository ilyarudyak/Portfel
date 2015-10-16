package com.ilyarudyak.android.portfel.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.ilyarudyak.android.portfel.data.PortfolioContract;
import com.ilyarudyak.android.portfel.utils.DataUtils;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by ilyarudyak on 10/6/15.
 */
public class MarketUpdateService extends IntentService {

    public static final String TAG = MarketUpdateService.class.getSimpleName();
    private static final long POLL_INTERVAL = AlarmManager.INTERVAL_HALF_DAY;

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

        Log.d(TAG, "network is available...");
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        Uri dirStockUri = PortfolioContract.StockTable.CONTENT_URI;

        // delete stocks from db before fetching
        cpo.add(ContentProviderOperation.newDelete(dirStockUri).build());

        // get indices and stocks from shared prefs; we store them in one table in DB
        String[] indexSymbols = PrefUtils.toArray(PrefUtils.getSymbols(getBaseContext(), PrefUtils.INDICES));
        String[] stockSymbols = PrefUtils.toArray(PrefUtils.getSymbols(getBaseContext(), PrefUtils.STOCKS));

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
}










