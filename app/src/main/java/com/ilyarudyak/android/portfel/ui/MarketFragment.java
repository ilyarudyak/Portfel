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
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.data.PortfolioContract;
import com.ilyarudyak.android.portfel.service.MarketUpdateService;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.ChartUtils;
import com.ilyarudyak.android.portfel.utils.DataUtils;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MarketFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = MarketFragment.class.getSimpleName();

    // used to transfer stock symbol to detail activity as intent extra
    public static final String SYMBOL = "com.ilyarudyak.android.portfel.ui.SYMBOL";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static String[] mIndexSymbols;
    private static String[] mStockSymbols;

    // used to build S&P500 chart
    private static Stock mIndexSnP500;

    /* these constants are used for recycler view with multiple parts:
    *  1) chart 2) header indices 3) indices 4) header stocks 5) stocks
    * */
    private static final int POSITION_CHART = 0;
    private static final int POSITION_HEADER_INDICES = 1;
    // 2 = 1 position for image + 1position for  indices header
    private static final int INDEX_POSITION_OFFSET = 2;
    // 3 = 1 position for image + 2 positions for headers
    private static final int ADDITIONAL_POSITIONS = 3;
    // this is not const - it depends on mIndexSymbols length
    private static int mPositionHeaderStock;

    public static MarketFragment newInstance() {
        return new MarketFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // we store symbols to show in the fragment in shared prefs and use them to fetch info from yahoo
        mIndexSymbols  = PrefUtils.toArray(PrefUtils.getSymbols(getActivity(), PrefUtils.INDEX));
        mStockSymbols = PrefUtils.toArray(PrefUtils.getSymbols(getActivity(), PrefUtils.STOCK));
        mPositionHeaderStock = INDEX_POSITION_OFFSET + mIndexSymbols.length;

        // we start service when: 1) alarm fires and 2) fragment started
        if (savedInstanceState == null) {
            refresh();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    // helper methods
    private void setRecyclerView(List<Stock> stocks) {
        Log.d(TAG, "setting recycler view...");
        if (stocks != null && stocks.size() > 0) {
            // set layout manager
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(llm);

            // set divider
            Drawable divider = getResources().getDrawable(R.drawable.padded_divider);
            mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration(divider));

            // set adapter
            MarketDataAdapter marketDataAdapter = new MarketDataAdapter(stocks);
            mRecyclerView.setAdapter(marketDataAdapter);
            Log.d(TAG, "setting recycler view DONE");
        }
    }
    private void refresh() {
        getActivity().startService(MarketUpdateService.newIntent(getActivity()));
    }

    // ------------------- RecyclerView classes -----------------

    private class MarketDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Stock> mIndicesAndStocks;

        public MarketDataAdapter(List<Stock> indicesAndStocks) {
            mIndicesAndStocks = indicesAndStocks;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            switch (viewType) {
                case R.id.view_holder_image:
                    view = getActivity().getLayoutInflater().inflate(
                            R.layout.list_item_market_line_chart, parent, false);
                    return new ChartViewHolder(getActivity(), view);
                case R.id.view_holder_header:
                    view = getActivity().getLayoutInflater().inflate(
                            R.layout.list_item_market_header, parent, false);
                    return new HeaderViewHolder(getActivity(), view);
                default:
                    view = getActivity().getLayoutInflater().inflate(
                            R.layout.list_item_market_stock, parent, false);
                    return new StockViewHolder(getActivity(), view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            Stock stock;
            if (position == POSITION_CHART) {
                ((ChartViewHolder) holder).bindModel();
            } else if (position == POSITION_HEADER_INDICES) {
                ((HeaderViewHolder) holder).bindModel(position);
            } else if (POSITION_HEADER_INDICES < position && position < mPositionHeaderStock) {
                stock = mIndicesAndStocks.get(position - INDEX_POSITION_OFFSET);
                ((StockViewHolder) holder).bindModel(stock);
            } else if (position == mPositionHeaderStock) {
                ((HeaderViewHolder) holder).bindModel(position);
            } else {
                stock = mIndicesAndStocks.get(position - ADDITIONAL_POSITIONS);
                ((StockViewHolder) holder).bindModel(stock);
            }
        }

        @Override
        public int getItemCount() {
            return mIndicesAndStocks.size() + ADDITIONAL_POSITIONS;
        }

        @Override
        public int getItemViewType(int position) {

            if(position == POSITION_CHART) {
                return R.id.view_holder_image;
            } else if (position == POSITION_HEADER_INDICES) {
                return R.id.view_holder_header;
            } else if (position == mPositionHeaderStock) {
                return R.id.view_holder_header;
            } else {
                return R.id.view_holder_stock;
            }
        }
    }
    public static class ChartViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private LineChart indexLineChart;

        public ChartViewHolder(Context context, View view) {
            super(view);
            this.context = context;
            indexLineChart = (LineChart) view.findViewById(R.id.market_list_item_line_chart);
        }

        public void bindModel() {
            new FetchIndexHistory(context, indexLineChart).execute();
        }
    }
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private Context context;

        public TextView headerTextView;

        public HeaderViewHolder(Context context, View view) {
            super(view);
            this.context = context;
            headerTextView = (TextView) view.findViewById(R.id.market_list_item_header);
        }

        public void bindModel(int headerPosition) {

            if (headerPosition == POSITION_HEADER_INDICES) {
                headerTextView.setText(context.getResources().getString(R.string.market_header_indices));
            } else if (headerPosition == mPositionHeaderStock) {
                headerTextView.setText(context.getResources().getString(R.string.market_header_stocks));
            }
        }
    }
    public class StockViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private Context context;

        public TextView symbolTextView;
        public TextView exchangeTextView;
        public ImageView changeIconImageView;
        public TextView priceTextView;
        public TextView changeAbsTextView;
        public TextView changePercentTextView;

        public StockViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            symbolTextView = (TextView) itemView.findViewById(R.id.list_item_stock_symbol);
            exchangeTextView = (TextView) itemView.findViewById(R.id.list_item_stock_exchange);
            changeIconImageView = (ImageView) itemView.findViewById(R.id.list_item_stock_change_icon);
            priceTextView = (TextView) itemView.findViewById(R.id.list_item_stock_price);
            changeAbsTextView = (TextView) itemView.findViewById(R.id.list_item_stock_change_absolute);
            changePercentTextView = (TextView) itemView.findViewById(R.id.list_item_stock_change_percent);

            itemView.setOnClickListener(this);
        }

        // if we work with index - get it's name from predefined list
        private String getNameForIndex(Stock stock) {
            String symbol = stock.getSymbol();
            String predefinedName;
            if (symbol.equals(getActivity().getResources().getString(R.string.index_symbol_sp500))) {
                predefinedName = getActivity().getResources().getString(R.string.index_symbol_sp500_name);
            } else if (symbol.equals(getActivity().getResources().getString(R.string.index_symbol_nasdaq))) {
                predefinedName = getActivity().getResources().getString(R.string.index_symbol_nasdaq_name);
            } else if (symbol.equals(getActivity().getResources().getString(R.string.index_symbol_nyse_amex))) {
                predefinedName = getActivity().getResources().getString(R.string.index_symbol_nyse_amex_name);
            } else {
                throw new IllegalArgumentException("unknown symbol");
            }
            return predefinedName;
        }

        public void bindModel(Stock stock) {

            String symbol;
            if (PrefUtils.isIndex(getActivity(), stock.getSymbol())) {
                symbol = getNameForIndex(stock) + " (" + stock.getStockExchange() + ")";

            } else {
                symbol = stock.getSymbol() + " (" + stock.getStockExchange() + ")";
            }
            symbolTextView.setText(symbol);

            String time = MiscUtils.formatTimeOnly(stock.getQuote().getLastTradeTime().getTime());
            exchangeTextView.setText(time);

            BigDecimal price = stock.getQuote().getPrice();
            priceTextView.setText(price.toString());

            BigDecimal changeAbs = stock.getQuote().getChange();
            changeAbsTextView.setText(MiscUtils.formatChanges(changeAbs, false));
            if (MiscUtils.isNonNegative(changeAbs)) {
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.accent));
                // return to standard position if cached item is used
                changeIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_change_history_black_24dp));
                changeIconImageView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.accent)));
            } else {
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.red));
                // change color to red and rotate 180 degrees
                changeIconImageView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                changeIconImageView.setRotation(180);
            }

            BigDecimal changePercent = stock.getQuote().getChangeInPercent();
            changePercentTextView.setText(MiscUtils.formatChanges(changePercent, true));
            if (MiscUtils.isNonNegative(changePercent)) {
                changePercentTextView.setTextColor(context.getResources().getColor(R.color.accent));
            } else {
                changePercentTextView.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        @Override
        public void onClick(View itemView) {
            Intent detailIntent = new Intent(getActivity(), StockDetailActivity.class);

            // set listener only for stocks (not indices)
            int adapterPosition = getAdapterPosition();
            if (adapterPosition > mPositionHeaderStock) {
                int index = getAdapterPosition() - mPositionHeaderStock - 1;
                detailIntent.putExtra(SYMBOL, mStockSymbols[index]);
                startActivity(detailIntent);
            }
        }
    }

    // ------------------- AsyncTask class -----------------

    /**
     * Fetch index (currently S&P500) data with history using yahoo finance
     * library. We then use historical data to build a yearly chart. We call
     * this task within recycler view adapter.
     * */
    private static class FetchIndexHistory extends AsyncTask<Void, Void, Void> {

        private final String TAG = FetchIndexHistory.class.getSimpleName();

        private Context context;
        private LineChart lineChart;

        public FetchIndexHistory(Context context, LineChart lineChart) {
            this.context = context;
            this.lineChart = lineChart;
        }

        @Override
        protected Void doInBackground(Void... ignore) {
            try {
                // this is the only place where we use context - to get string resources
                String snp500Str = context.getResources().getString(R.string.index_symbol_sp500);
                mIndexSnP500 = YahooFinance.get(snp500Str, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void ignore) {
            try {
                if (mIndexSnP500 != null) {
                    ChartUtils.buildLineChart(context, lineChart, mIndexSnP500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------ loader methods ------------------
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // we store indices and stocks in the same table -
        // indices before stocks and fetch them in this order
        String sortOrder = BaseColumns._ID;
        return new CursorLoader(getActivity(),
                PortfolioContract.StockTable.CONTENT_URI,
                null, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished() is called...");
        if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "cursor is not null and count > 0");
            setRecyclerView(DataUtils.buildStockList(cursor));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

}