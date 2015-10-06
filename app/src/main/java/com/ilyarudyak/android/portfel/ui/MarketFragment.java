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
import android.content.Context;
import android.content.Intent;
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

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.api.Config;
import com.ilyarudyak.android.portfel.data.MarketUpdateService;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
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

    private static final int POSITION_IMAGE = 0;
    private static final int POSITION_HEADER_INDICES = 1;
    private static final int POSITION_HEADER_STOCKS = 5;
    private static final int ADDITIONAL_POSITIONS_BEFORE_STOCKS_HEADER = 2;
    private static final int ADDITIONAL_POSITIONS = 3;

    private static List<String> SYMBOLS = new ArrayList<>(Arrays.asList("GOOG", "AAPL", "YHOO", "IBM", "FB",
            "INTC", "ORCL", "HPQ", "TSLA", "MSFT"));

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
    public void onStart() {
        super.onStart();
        String[] symbols = SYMBOLS.toArray(new String[SYMBOLS.size()]);
        new FetchMarketData().execute(symbols);

        Log.d(TAG, "i'm going to start service...");
        Intent intent = new Intent(getActivity(), MarketUpdateService.class);
        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_market, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.market_recycler_view);

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

    private class MarketDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Stock> mStocks;

        public MarketDataAdapter(List<Stock> stocks) {
            mStocks = stocks;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            switch (viewType) {
                case R.id.view_holder_image:
                    view = getActivity().getLayoutInflater().inflate(
                            R.layout.list_item_market_image, parent, false);
                    return new ImageViewHolder(getActivity(), view);
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
            if (position == POSITION_IMAGE) {
                ((ImageViewHolder) holder).bindModel();
            } else if (position == POSITION_HEADER_INDICES || position == POSITION_HEADER_STOCKS) {
                ((HeaderViewHolder) holder).bindModel(position);
            } else if (POSITION_HEADER_INDICES < position && position < POSITION_HEADER_STOCKS) {
                stock = mStocks.get(position - ADDITIONAL_POSITIONS_BEFORE_STOCKS_HEADER);
                ((StockViewHolder) holder).bindModel(stock);
            } else {
                stock = mStocks.get(position - ADDITIONAL_POSITIONS);
                ((StockViewHolder) holder).bindModel(stock);
            }
        }

        @Override
        public int getItemCount() {
            return mStocks.size() + ADDITIONAL_POSITIONS;
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case POSITION_IMAGE:
                    return R.id.view_holder_image;
                case POSITION_HEADER_INDICES:
                    return R.id.view_holder_header;
                case POSITION_HEADER_STOCKS:
                    return R.id.view_holder_header;
                default:
                    return R.id.view_holder_stock;
            }
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
                    .load(Config.S_AND_P_URL.toString())
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

            switch (headerPosition) {
                case POSITION_HEADER_INDICES:
                    headerTextView.setText(context.getResources().getString(R.string.market_header_indices));
                    break;
                case POSITION_HEADER_STOCKS:
                    headerTextView.setText(context.getResources().getString(R.string.market_header_stocks));
                    break;
            }
        }
    }
    public class StockViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private Context context;

        public TextView symbolTextView;
        public TextView exchangeTextView;
        public TextView priceTextView;
        public TextView changeAbsTextView;
        public TextView changePercentTextView;

        public StockViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            symbolTextView = (TextView) itemView.findViewById(R.id.list_item_stock_symbol);
            exchangeTextView = (TextView) itemView.findViewById(R.id.list_item_stock_exchange);
            priceTextView = (TextView) itemView.findViewById(R.id.list_item_stock_price);
            changeAbsTextView = (TextView) itemView.findViewById(R.id.list_item_stock_change_absolute);
            changePercentTextView = (TextView) itemView.findViewById(R.id.list_item_stock_change_percent);

            itemView.setOnClickListener(this);
        }

        public void bindModel(Stock stock) {

            String symbol = stock.getSymbol();
            symbolTextView.setText(symbol);

            String exchange = stock.getStockExchange();
            exchangeTextView.setText(exchange);

            BigDecimal price = stock.getQuote().getPrice();
            priceTextView.setText(price.toString());

            BigDecimal changeAbs = stock.getQuote().getChange();
            changeAbsTextView.setText(MiscUtils.formatChanges(changeAbs, false));
            if (MiscUtils.isNonNegative(changeAbs)) {
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.primary));
            } else {
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.red));
            }

            BigDecimal changePercent = stock.getQuote().getChangeInPercent();
            changePercentTextView.setText(MiscUtils.formatChanges(changePercent, true));
            if (MiscUtils.isNonNegative(changePercent)) {
                changePercentTextView.setTextColor(context.getResources().getColor(R.color.primary));
            } else {
                changePercentTextView.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        @Override
        public void onClick(View itemView) {
//            Item rssItem = mFeed.getItems().get(getAdapterPosition());
//            String itemUrlStr = rssItem.getLink();
            Intent detailIntent = new Intent(getActivity(), StockDetailActivity.class);
//            detailIntent.putExtra(NewsDetailActivity.EXTRA_RSS_ITEM_URL_STRING, itemUrlStr);
            startActivity(detailIntent);
        }
    }


}