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

import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.api.Config;
import com.ilyarudyak.android.portfel.data.MarketUpdateService;
import com.ilyarudyak.android.portfel.data.PortfolioContract;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.DataUtils;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MarketFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = MarketFragment.class.getSimpleName();
    private static final String KEY_POSITION = "com.ilyarudyak.android.portfel.ui.POSITION";

    private static String[] mIndexSymbols;
    private static String[] mStockSymbols;

    private static final int POSITION_IMAGE = 0;
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

//        new FetchMarketData().execute(concat(mIndexSymbols, mStockSymbols));

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
//                mStockSymbols.add("TWTR");
//                String[] symbols = mStockSymbols.toArray(new String[mStockSymbols.size()]);
//                new FetchMarketData().execute(symbols);
            }
        });


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    // helper methods
    private void setRecyclerView(List<Stock> stocks) {

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
        }
    }
    private String[] concat(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c= new String[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    // ------------------- AsyncTask classes -----------------

    private class FetchMarketData extends AsyncTask<String, Void, List<Stock>> {

        @Override
        protected List<Stock> doInBackground(String ...symbols) {

            Map<String, Stock> indicesAndStocks = new HashMap<>();
            try {
                indicesAndStocks = YahooFinance.get(symbols);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return getList(indicesAndStocks);
        }

        @Override
        protected void onPostExecute(List<Stock> indicesAndStocks) {
            setRecyclerView(indicesAndStocks);
        }

        private List<Stock> getList(Map<String, Stock> indicesAndStocks) {
            List<Stock> list = new ArrayList<>();

            for (String symbol: mIndexSymbols) {
                list.add(indicesAndStocks.get(symbol));
            }

            for (String symbol: mStockSymbols) {
                list.add(indicesAndStocks.get(symbol));
            }

            return list;
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

            if(position == POSITION_IMAGE) {
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

            BigDecimal price = new BigDecimal("0");//stock.getQuote().getPrice();
            priceTextView.setText(price.toString());

            BigDecimal changeAbs = new BigDecimal("0");//stock.getQuote().getChange();
            changeAbsTextView.setText(MiscUtils.formatChanges(changeAbs, false));
            if (MiscUtils.isNonNegative(changeAbs)) {
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.accent));
            } else {
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.red));
            }

            BigDecimal changePercent = new BigDecimal("0");//stock.getQuote().getChangeInPercent();
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
            startActivity(detailIntent);
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
        Log.d(TAG, "i'm done");
        if (cursor != null) {
            setRecyclerView(DataUtils.buildStockList(cursor));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

}