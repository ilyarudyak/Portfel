package com.ilyarudyak.android.portfel.utils;

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
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
        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        set1.setColor(context.getResources().getColor(R.color.accent));
        set1.setCircleColor(context.getResources().getColor(R.color.accent));
        set1.setLineWidth(2f);
        set1.setCircleSize(4f);
        set1.setDrawValues(false);


        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);

        // set chart properties
        // (a) remove legend and description
        chart.getLegend().setEnabled(false);
        chart.setDescription("");

        // (b) remove right Y-axis and line from right Y-axis
        // and auto-adjust left Y-axis
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setStartAtZero(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.setAutoScaleMinMaxEnabled(true);

        // (c) remove grid, add line of X-axis, change position
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawAxisLine(true);

        // set data on chart
        chart.setData(data);
        chart.invalidate();
    }
}
