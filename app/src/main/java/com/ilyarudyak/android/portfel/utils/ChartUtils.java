package com.ilyarudyak.android.portfel.utils;

import android.content.Context;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ilyarudyak.android.portfel.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * Created by ilyarudyak on 10/12/15.
 */
public class ChartUtils {

    public static void buildLineChart(Context context, LineChart chart, Stock stock) throws IOException {

        // get historical data from stock
        List<HistoricalQuote> history = stock.getHistory();

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
    }

    public static void buildCandleStickChart(Context context, CandleStickChart chart, Stock stock) throws IOException {

        // get historical data from stock
        List<HistoricalQuote> history = stock.getHistory();

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

        setChartProperties(context, chart, stock);

        // set additional properties
        chart.setAutoScaleMinMaxEnabled(false);

        chart.setData(data);
        chart.invalidate();
    }

    private static void setChartProperties(Context context, BarLineChartBase chart, Stock stock) {
        // set chart properties
        // (a) change legend place and set description
        chart.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);
        chart.getLegend().setTextColor(context.getResources().getColor(R.color.primary_semi_light));
        chart.setDescription("previous close: " + stock.getQuote().getPreviousClose());
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















