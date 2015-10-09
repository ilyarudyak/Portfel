package com.ilyarudyak.android.portfel.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.ilyarudyak.android.portfel.utils.DataUtils;
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

    public MarketUpdateService() {
        super(TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "starting service...");
        ArrayList<ContentProviderOperation> cpo = new ArrayList<>();

        // delete stocks from db before fetching
        Uri dirUri = PortfolioContract.StockTable.CONTENT_URI;
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        // get stocks from shared prefs
        String[] stockSymbols = PrefUtils.toArray(PrefUtils.getSymbols(getBaseContext(), PrefUtils.STOCKS));

        // fetch new information about stocks
        Map<String, Stock> stocks = null;
        try {
            stocks = YahooFinance.get(stockSymbols);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // insert this stocks into db
        if (stocks != null) {
            for (Stock stock: stocks.values()) {
                ContentValues values = DataUtils.buildContentValues(stock);
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }
            try {
                getContentResolver().applyBatch(PortfolioContract.CONTENT_AUTHORITY, cpo);
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "inserting data into db...");
        }
    }
}
