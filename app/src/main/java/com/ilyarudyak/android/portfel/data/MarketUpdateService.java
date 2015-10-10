package com.ilyarudyak.android.portfel.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
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

        Uri dirStockUri = PortfolioContract.StockTable.CONTENT_URI;
        Uri dirStockQuoteUri = PortfolioContract.StockQuoteTable.CONTENT_URI;

        // delete stocks from db before fetching
        getContentResolver().delete(dirStockUri, null, null);
        getContentResolver().delete(dirStockQuoteUri, null, null);

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
                ContentValues valuesStock = DataUtils.buildContentValuesStock(stock);
                Uri uri = getContentResolver().insert(dirStockUri, valuesStock);
                String id = PortfolioContract.StockTable.getStockId(uri);
                ContentValues valuesStockQuotes = DataUtils.buildContentValuesStockQuote(stock, Integer.parseInt(id));
                getContentResolver().insert(dirStockQuoteUri, valuesStockQuotes);
            }

            Log.d(TAG, "inserting data into db...");
        }
    }
}
