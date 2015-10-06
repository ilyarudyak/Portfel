package com.ilyarudyak.android.portfel.data;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ilyarudyak.android.portfel.utils.DataUtils;

import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by ilyarudyak on 10/6/15.
 */
public class MarketUpdateService extends IntentService {

    public static final String TAG = MarketUpdateService.class.getSimpleName();

    public MarketUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // delete stocks from db
        Uri uri = PortfolioContract.StockTable.CONTENT_URI;
        getContentResolver().delete(uri, null, null);

        // fetch new information about stocks
        Stock stock = null;
        try {
            stock = YahooFinance.get("TSLA");
            Log.d(TAG, "fetching data...");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // insert this stocks into db
        if (stock != null) {
            getContentResolver().insert(uri, DataUtils.buildContentValues(stock));
            Log.d(TAG, "insert data into db...");
        }
    }
}
