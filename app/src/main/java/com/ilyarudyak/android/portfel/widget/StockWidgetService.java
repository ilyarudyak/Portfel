package com.ilyarudyak.android.portfel.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.ui.MainActivity;
import com.ilyarudyak.android.portfel.utils.MiscUtils;

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
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.list_item_market_stock);
        bindRemoteViews();
        buildIntent();
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
    }
    private void buildIntent() {
        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.list_item_stock_grid_layout, pendingIntent);
    }
    private void fetchStock() {
        try {
            mStock = YahooFinance.get(mSymbol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void bindRemoteViews() {

        if (mStock != null) {
            mRemoteViews.setTextViewText(R.id.list_item_stock_symbol_exchange, mStock.getSymbol() +
                    " (" + mStock.getStockExchange() + ")");
            String timeStr = MiscUtils.formatTimeOnly(mStock.getQuote().getLastTradeTime().getTime());
            mRemoteViews.setTextViewText(R.id.list_item_stock_time, timeStr);

            mRemoteViews.setTextViewText(R.id.list_item_stock_price, mStock.getQuote().getPrice().toString());

            String changeAbsStr = MiscUtils.formatChanges(mStock.getQuote().getChange(), false);
            mRemoteViews.setTextViewText(R.id.list_item_stock_change_absolute, changeAbsStr);
            String changeAbsPercent = MiscUtils.formatChanges(mStock.getQuote().getChangeInPercent(), true);
            mRemoteViews.setTextViewText(R.id.list_item_stock_change_percent, changeAbsPercent);
        }
    }

}
