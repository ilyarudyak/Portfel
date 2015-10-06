package com.ilyarudyak.android.portfel.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.utils.MiscUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;

public class StockDetailActivity extends AppCompatActivity {

    private static final String TAG = StockDetailActivity.class.getSimpleName();
    private LineChart mChart;
    private Stock mTesla;

    @Override
    protected void onStart() {
        super.onStart();
        new FetchStockHistory().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        mChart = (LineChart) findViewById(R.id.chart);


    }

    // helper methods
    private void setChart() throws IOException {

        // get historical data from stock
        List<HistoricalQuote> history = mTesla.getHistory();

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
        set1.setColor(getResources().getColor(R.color.accent));
        set1.setCircleColor(getResources().getColor(R.color.accent));
        set1.setLineWidth(2f);
        set1.setCircleSize(4f);
        set1.setDrawValues(false);


        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);

        // set chart properties
        // (a) remove legend and description
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");

        // (b) remove right Y-axis and line from right Y-axis
        // and auto-adjust left Y-axis
        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setStartAtZero(false);
        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.setAutoScaleMinMaxEnabled(true);

        // (c) remove grid, add line of X-axis, change position
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.getXAxis().setDrawAxisLine(true);

        // set data on chart
        mChart.setData(data);
        mChart.invalidate();
    }

    private class FetchStockHistory extends AsyncTask<Void, Void, Void> {

        private final String TAG = FetchStockHistory.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... ignore) {
            try {
                mTesla = YahooFinance.get("TSLA", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void ignore) {
            try {
                Log.d(TAG, mTesla.getHistory().get(0).getClose().toString());
                setChart();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}













