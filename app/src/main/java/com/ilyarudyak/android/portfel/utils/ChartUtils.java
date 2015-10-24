package com.ilyarudyak.android.portfel.utils;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.ilyarudyak.android.portfel.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * We build three types of charts:
 * 1) line chart to represent S&P 500 on the market screen;
 * 2) candlestick chart to represent a stock on stock details screen;
 * 3) bar chart to show portfolio performance on portfolio screen.
 *
 * We have a few serious limitations for these charts:
 * a) we use only yearly data for charts 1-2 and
 * b) we use test data for chart 3.
 */
public class ChartUtils {

    public static final int MONTHS = 12;
    public static final int PORTFOLIO_MIN_VALUE = 15000;
    public static final int PORTFOLIO_INTERVAL = 1000;
    private static final String TAG = ChartUtils.class.getSimpleName();

    public static void buildLineChart(Context context, LineChart chart, Stock stock) throws IOException {

        // get historical data from stock
        List<HistoricalQuote> history = stock.getHistory();
        
        // we have to reverse list - original list goes from recent to old items
        Collections.reverse(history);

        // set x axis values
        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            xVals.add(MiscUtils.formatMonthOnly(history.get(i).getDate().getTime()));
        }

        // set y axis values
        ArrayList<Entry> yVals = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {

            float val = history.get(i).getClose().floatValue();
            yVals.add(new Entry(val, i));
        }

        // create data set
        LineDataSet set1 = new LineDataSet(yVals, stock.getName());
        set1.setColor(context.getResources().getColor(R.color.primary));
        set1.setLineWidth(2f);
        set1.setCircleSize(0f);
        set1.setDrawValues(false);


        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);

        setChartProperties(context, chart, stock);

        // set data on chart
        chart.setData(data);
        chart.invalidate();
        chart.notifyDataSetChanged();
    }
    public static void buildCandleStickChart(Context context, CandleStickChart chart, Stock stock) throws IOException {

        // get historical data from stock
        List<HistoricalQuote> history = stock.getHistory();

        // we have to reverse list - original list goes from recent to old items
        Collections.reverse(history);

        List<CandleEntry> yVals1 = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            HistoricalQuote hq = history.get(i);
            CandleEntry entry = new CandleEntry(i, hq.getHigh().intValue(), hq.getLow().intValue(),
                            hq.getOpen().intValue(), hq.getClose().intValue());
            yVals1.add(entry);
        }

        List<String> xVals = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            xVals.add(MiscUtils.formatMonthOnly(history.get(i).getDate().getTime()));
        }

        CandleDataSet set1 = new CandleDataSet(yVals1, stock.getName());
        set1.setDrawValues(false);
        set1.setColor(context.getResources().getColor(R.color.primary));
        set1.setShadowColor(context.getResources().getColor(R.color.primary));
        set1.setDecreasingColor(context.getResources().getColor(R.color.red));
        set1.setIncreasingColor(context.getResources().getColor(R.color.accent));
        set1.setBodySpace(.3f);

        CandleData data = new CandleData(xVals, set1);

        // set chart properties
        setChartProperties(context, chart, stock);
        chart.setAutoScaleMinMaxEnabled(false);

        chart.setData(data);
        chart.invalidate();
        chart.notifyDataSetChanged();
    }
    public static void buildBarChart(Context context, BarChart chart) throws IOException {

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.MONTH, -MONTHS);

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i <= MONTHS; i++) {
            String month = MiscUtils.formatMonthOnly(calendar.getTime());
            calendar.add(Calendar.MONTH, 1);
            Log.d(TAG, "month=" + month);
            xVals.add(month);
        }

        ArrayList<BarEntry> yVals1 = new ArrayList<>();

        Random random = new Random(0);
        for (int i = 0; i <= MONTHS; i++) {
            yVals1.add(new BarEntry(PORTFOLIO_MIN_VALUE + random.nextInt(PORTFOLIO_INTERVAL), i));
        }

        BarDataSet set1 = new BarDataSet(yVals1, context.getString(R.string.portfolio_chart_description));
        set1.setBarSpacePercent(35f);
        set1.setDrawValues(false);
        set1.setColor(context.getResources().getColor(R.color.primary_semi_light));

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        // set chart properties
        setChartProperties(context, chart, null);
        chart.setDescription("");
        YAxisValueFormatter formatter = new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                value /= 1000;
                return String.format("%.1f", value);
            }
        };
        chart.getAxisLeft().setValueFormatter(formatter);
        chart.animateY(2000);

        chart.setData(data);
        chart.invalidate();
        chart.notifyDataSetChanged();
    }

    // helper methods
    private static void setChartProperties(Context context, BarLineChartBase chart, Stock stock) {
        // set chart properties
        // (a) change legend place and set description
        chart.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.primary_semi_light));
        if (stock != null) {
            chart.setDescription("previous close: " + stock.getQuote().getPreviousClose());
        }
        chart.setDescriptionColor(context.getResources().getColor(R.color.primary_semi_light));

        // (b) remove right Y-axis and line from right Y-axis
        // and auto-adjust left Y-axis
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setStartAtZero(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setTextColor(context.getResources().getColor(R.color.primary));
        chart.setAutoScaleMinMaxEnabled(true);

        // (c) remove grid, add line of X-axis, change position
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setGridColor(context.getResources().getColor(R.color.primary_semi_light));
        chart.getXAxis().setTextColor(context.getResources().getColor(R.color.primary));

        // (d) set background color
        chart.setGridBackgroundColor(context.getResources().getColor(R.color.primary_light));
        chart.setBackgroundColor(context.getResources().getColor(R.color.primary_light));
    }
}















