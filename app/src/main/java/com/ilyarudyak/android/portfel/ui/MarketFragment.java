/***
 * Copyright (c) 2012-14 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 * <p/>
 * From _The Busy Coder's Guide to Android Development_
 * https://commonsware.com/Android
 */

package com.ilyarudyak.android.portfel.ui;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.api.Config;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MarketFragment extends Fragment {

    public static final String TAG = MarketFragment.class.getSimpleName();
    private static final String KEY_POSITION = "com.ilyarudyak.android.portfel.ui.POSITION";

    private static List<String> SYMBOLS = new ArrayList<>(Arrays.asList("%5EGSPC", "GOOG", "AAPL", "YHOO", "IBM", "FB",
            "INTC", "ORCL", "HPQ", "TSLA", "MSFT"));

    private RecyclerView mRecyclerView;
    private ImageView mIndexChartImageView;
    private FloatingActionButton mFAB;

    public static MarketFragment newInstance(int position) {

        MarketFragment pf = new MarketFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        pf.setArguments(args);

        return pf;
    }

    @Override
    public void onStart() {
        super.onStart();
        String[] symbols = SYMBOLS.toArray(new String[SYMBOLS.size()]);
        new FetchMarketData().execute(symbols);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_market, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.market_recycler_view);
        mIndexChartImageView = (ImageView) view.findViewById(R.id.market_index_chart_image_view);

        Picasso.with(getActivity())
                .load(Config.S_AND_P_URL.toString())
                .into(mIndexChartImageView);

        mFAB = (FloatingActionButton) view.findViewById(R.id.market_fab_add_stock);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SYMBOLS.add("TWTR");
                String[] symbols = SYMBOLS.toArray(new String[SYMBOLS.size()]);
                new FetchMarketData().execute(symbols);
            }
        });

        return view;
    }

    // helper methods
    private void setRecyclerView(List<Stock> stocks) {

        // set layout manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);

        // set divider
        Drawable divider = getResources().getDrawable(R.drawable.padded_divider);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration(divider));

        // set adapter
        MarketDataAdapter marketDataAdapter = new MarketDataAdapter(stocks);
        mRecyclerView.setAdapter(marketDataAdapter);
    }

    // ------------------- AsyncTask classes -----------------

    private class FetchMarketData extends AsyncTask<String, Void, List<Stock>> {

        @Override
        protected List<Stock> doInBackground(String ...symbols) {

            Map<String, Stock> stocks = new HashMap<>();
            try {
                stocks = YahooFinance.get(symbols);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new ArrayList<>(stocks.values());
        }

        @Override
        protected void onPostExecute(List<Stock> stocks) {
            setRecyclerView(stocks);
        }
    }

    // ------------------- RecyclerView classes -----------------

    private class MarketDataAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<Stock> mStocks;

        public MarketDataAdapter(List<Stock> stocks) {
            mStocks = stocks;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = getActivity().getLayoutInflater().inflate(
                    R.layout.list_item_stock, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Stock stock = mStocks.get(position);

            String symbol = stock.getSymbol();
            holder.symbolTextView.setText(symbol);

            String exchange = stock.getStockExchange();
            holder.exchangeTextView.setText(exchange);

            BigDecimal price = stock.getQuote().getPrice();
            holder.priceTextView.setText(price.toString());

            BigDecimal changeAbs = stock.getQuote().getChange();
            holder.changeAbsTextView.setText(Utils.formatChanges(changeAbs, false));
            if (Utils.isNonNegative(changeAbs)) {
                holder.changeAbsTextView.setTextColor(getResources().getColor(R.color.primary));
            } else {
                holder.changeAbsTextView.setTextColor(getResources().getColor(R.color.red));
            }

            BigDecimal changePercent = stock.getQuote().getChangeInPercent();
            holder.changePercentTextView.setText(Utils.formatChanges(changePercent, true));
            if (Utils.isNonNegative(changePercent)) {
                holder.changePercentTextView.setTextColor(getResources().getColor(R.color.primary));
            } else {
                holder.changePercentTextView.setTextColor(getResources().getColor(R.color.red));
            }
        }

        @Override
        public int getItemCount() {
            return mStocks.size();
        }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView symbolTextView;
        public TextView exchangeTextView;
        public TextView priceTextView;
        public TextView changeAbsTextView;
        public TextView changePercentTextView;

        public ViewHolder(View view) {
            super(view);
            symbolTextView = (TextView) view.findViewById(R.id.list_item_stock_symbol);
            exchangeTextView = (TextView) view.findViewById(R.id.list_item_stock_exchange);
            priceTextView = (TextView) view.findViewById(R.id.list_item_stock_price);
            changeAbsTextView = (TextView) view.findViewById(R.id.list_item_stock_change_absolute);
            changePercentTextView = (TextView) view.findViewById(R.id.list_item_stock_change_percent);
        }
    }
}