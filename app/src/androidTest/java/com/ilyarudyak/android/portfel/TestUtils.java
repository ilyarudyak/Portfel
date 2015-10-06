package com.ilyarudyak.android.portfel;

import android.content.ContentValues;

import com.ilyarudyak.android.portfel.data.PortfolioContract;

/**
 * Created by ilyarudyak on 10/6/15.
 */
public class TestUtils {

    public static ContentValues createTeslaContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(PortfolioContract.StockTable.SYMBOL, "TSLA");
        cv.put(PortfolioContract.StockTable.NAME, "Tesla");
        cv.put(PortfolioContract.StockTable.CURRENCY, "USD");
        cv.put(PortfolioContract.StockTable.STOCK_EXCHANGE, "NSE");

        return cv;
    }
}
