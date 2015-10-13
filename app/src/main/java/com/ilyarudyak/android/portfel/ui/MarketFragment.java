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
import android.support.design.widget.FloatingActionButton;
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
import com.ilyarudyak.android.portfel.api.Config;
import com.ilyarudyak.android.portfel.data.PortfolioContract;
import com.ilyarudyak.android.portfel.service.MarketUpdateService;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.ChartUtils;
import com.ilyarudyak.android.portfel.utils.DataUtils;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MarketFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = MarketFragment.class.getSimpleName();
    public static final String KEY_POSITION = "com.ilyarudyak.android.portfel.ui.POSITION";
    public static final String SYMBOL = "com.ilyarudyak.android.portfel.ui.SYMBOL";

    private static String[] mIndexSymbols;
    private static String[] mStockSymbols;

    private static Stock mIndexSnP500;

    private static final int POSITION_CHART = 0;
    private static final int POSITION_HEADER_INDICES = 1;
    // image and indices header before
    private static final int INDEX_POSITION_OFFSET = 2;
    // 1 position - image, 2 position - headers
    private static final int ADDITIONAL_POSITIONS = 3;

    // this is not const - it depends on mIndexSymbols length
    private static int mPositionHeaderStock;

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFAB;

    public static MarketFragment newInstance(int position) {

        MarketFragment pf = new MarketFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        pf.setArguments(args);

        return pf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIndexSymbols  = PrefUtils.toArray(PrefUtils.getSymbols(getActivity(), PrefUtils.INDICES));
        mStockSymbols = PrefUtils.toArray(PrefUtils.getSymbols(getActivity(), PrefUtils.STOCKS));
        mPositionHeaderStock = INDEX_POSITION_OFFSET + mIndexSymbols.length;

        getActivity().startService(MarketUpdateService.newIntent(getActivity()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        getLoaderManager().initLoader(0, null, this);
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
    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        public ImageView indexPlotImageView;

        public ImageViewHolder(Context context, View view) {
            super(view);
            this.context = context;
            indexPlotImageView = (ImageView) view.findViewById(R.id.market_list_item_image_view);

        }

        public void bindModel() {

            Picasso.with(context)
                    .load(Config.SP500_URL.toString())
                    .into(indexPlotImageView);
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

        public void bindModel(Stock stock) {

            String symbol = stock.getSymbol() + " (NYQ)";
            symbolTextView.setText(symbol);

            String exchange = "14:15 pm";
            exchangeTextView.setText(exchange);

            BigDecimal price = stock.getQuote().getPrice();
            priceTextView.setText(price.toString());
//            Log.d(TAG, stock.getQuote().getPreviousClose().toString());

            BigDecimal changeAbs = stock.getQuote().getChange();
            changeAbsTextView.setText(MiscUtils.formatChanges(changeAbs, false));
            if (MiscUtils.isNonNegative(changeAbs)) {
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.accent));
                // return to standard position if cached item is used
                float rotation = changeIconImageView.getRotation();
                if (rotation != 0) {
                    changeIconImageView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.accent)));
                    changeIconImageView.setRotation(180);
                }
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
            int index = getAdapterPosition() - mPositionHeaderStock - 1;
            detailIntent.putExtra(SYMBOL, mStockSymbols[index]);
            startActivity(detailIntent);
        }
    }

    // ------------------- AsyncTask class -----------------

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
                String snp500Str = "TSLA";//context.getResources().getString(R.string.index_symbol_sp500);
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
                    Log.d(TAG, mIndexSnP500.getHistory().get(0).getClose().toString());
                    ChartUtils.buildLineChart(context, lineChart, mIndexSnP500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ------------------ loader methods ------------------

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                PortfolioContract.StockTable.CONTENT_URI,
                null, null, null, null);
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