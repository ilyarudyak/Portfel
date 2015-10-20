package com.ilyarudyak.android.portfel.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.ui.MainActivity;

import java.io.IOException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 *
 */
public class StockWidgetService extends IntentService {

    public static final String TAG = StockWidgetService.class.getSimpleName();

    private RemoteViews mRemoteViews;
    private Stock mStock;
    private String mSymbol = "TSLA";

    public StockWidgetService() {
        super(StockWidgetService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // fetch stock info
        fetchStock();

        // retrieve all of the widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, StockWidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {
            setRemoteViews(appWidgetManager, appWidgetId);
        }
    }

    // helper functions
    private void setRemoteViews(AppWidgetManager appWidgetManager, int appWidgetId) {
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.widget_stock);
        bindRemoteViews();
        buildIntent();
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
    }
    private void buildIntent() {
        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.widget_stock_relative_layout, pendingIntent);
    }
    private void fetchStock() {
        try {
            mStock = YahooFinance.get(mSymbol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void bindRemoteViews() {

        Log.d(TAG, "trying to set remote views");
        Log.d(TAG, "stock==null " + (mStock == null));

        if (mStock != null) {
            Log.d(TAG, "price=" + mStock.getQuote().getPrice());
            // set text fields
            mRemoteViews.setTextViewText(R.id.widget_stock_symbol, mStock.getSymbol() + " (" + mStock.getStockExchange() + ")");
            mRemoteViews.setTextViewText(R.id.widget_stock_price, mStock.getQuote().getPrice().toString());
            String changeStr = mStock.getQuote().getChange() + " (" + mStock.getQuote().getChangeInPercent() + ")";
            mRemoteViews.setTextViewText(R.id.widget_stock_change, changeStr);

            // set chart
//        mRemoteViews.setTextViewText(R.id.time_text_view, mStock.getTime());
        }
    }

}
