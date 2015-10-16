package com.ilyarudyak.android.portfel;

import android.test.AndroidTestCase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by ilyarudyak on 10/16/15.
 */
public class TestMisc extends AndroidTestCase {

    private static final String TAG = TestMisc.class.getSimpleName();

    public void testInsert() throws Throwable {

        String symbol = "^GSPC";
        Stock stock = YahooFinance.get(symbol);
        Date date = stock.getQuote().getLastTradeTime().getTime();
        String dateStr = formatDate(date);
        Log.d(TAG, "symbol=" + stock.getSymbol() +
                " calendar=" + dateStr +
                " time=" + stock.getQuote().getLastTradeTimeStr() +
                " date=" + stock.getQuote().getLastTradeDateStr());
    }

    private String formatDate(Date date) {
        String template = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(template);
        return sdf.format(date);
    }
}
