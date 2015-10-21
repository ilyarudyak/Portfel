package com.ilyarudyak.android.portfel;

import android.test.AndroidTestCase;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.CandleStickChart;

/**
 * Created by ilyarudyak on 10/16/15.
 */
public class TestMisc extends AndroidTestCase {

    private static final String TAG = TestMisc.class.getSimpleName();

/*    public void testInsert() throws Throwable {

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
    }*/

    public void testChart() throws Throwable {
        RelativeLayout rl = new RelativeLayout(mContext);
        rl.setLayoutParams(new RelativeLayout.LayoutParams(180, 120));
        CandleStickChart chart = new CandleStickChart(mContext);
        rl.addView(chart);
        chart.requestLayout();
        chart.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Log.d(TAG, "rlw=" + rl.getWidth());
    }
}
