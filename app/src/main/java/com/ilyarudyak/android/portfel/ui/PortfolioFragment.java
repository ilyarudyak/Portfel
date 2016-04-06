package com.ilyarudyak.android.portfel.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.ChartUtils;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class PortfolioFragment extends Fragment {

    public static final String TAG = PortfolioFragment.class.getSimpleName();

    private List<Stock> mStocks;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String[] mStockSymbols;

    /**
     * these constants are used for recycler view with multiple parts:
     *  1) chart 2) header stocks 3) stocks
     * */
    private static final int POSITION_CHART =               0;
    private static final int POSITION_HEADER_STOCKS =       1;
    // 2 = 1 position for image + 1 positions for header
    private static final int ADDITIONAL_POSITIONS =         2;

    public static PortfolioFragment newInstance() {
        return new PortfolioFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStockSymbols = PrefUtils.toArray(PrefUtils.getPortfolioStocks(getActivity()));
        if (savedInstanceState == null) {
            fetchDataWithAsyncTask();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setupRecyclerView();

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataWithAsyncTask();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    // helper methods
    private void setupRecyclerView() {

        // set layout manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);

        // set divider
        Drawable divider = getResources().getDrawable(R.drawable.padded_divider);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration(divider));
    }
    private void setupAdapter() {
        if (isAdded()){
            if (mStocks != null && mStocks.size() > 0) {
                mRecyclerView.setAdapter(new PortfolioDataAdapter());
            }
        }
    }
    private void fetchDataWithAsyncTask() {
        new FetchStocksTask().execute(mStockSymbols);
    }

    // --------------------- recycler view ------------------------

    private class PortfolioDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case R.id.view_holder_image:
                    view = getActivity().getLayoutInflater().inflate(
                            R.layout.list_item_porfolio_bar_chart, parent, false);
                    return new ChartViewHolder(getActivity(), view);
                case R.id.view_holder_header:
                    // we reuse market header
                    view = getActivity().getLayoutInflater().inflate(
                            R.layout.list_item_market_header, parent, false);
                    return new HeaderViewHolder(getActivity(), view);
                default:
                    // we reuse market stock
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
            } else if (position == POSITION_HEADER_STOCKS) {
                ((HeaderViewHolder) holder).bindModel();
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

            if (position == POSITION_CHART) {
                return R.id.view_holder_image;
            } else if (position == POSITION_HEADER_STOCKS) {
                return R.id.view_holder_header;
            } else {
                return R.id.view_holder_stock;
            }
        }
    }
    private static class ChartViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private BarChart portfolioBarChart;

        public ChartViewHolder(Context context, View view) {
            super(view);
            this.context = context;
            portfolioBarChart = (BarChart) view.findViewById(R.id.list_item_portfolio_bar_chart);
        }

        public void bindModel() {
            try {
                ChartUtils.buildBarChart(context, portfolioBarChart);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private TextView headerTextView;

        public HeaderViewHolder(Context context, View view) {
            super(view);
            this.context = context;
            // we reuse header layout from market fragment
            headerTextView = (TextView) view.findViewById(R.id.market_list_item_header);
        }

        public void bindModel() {
            headerTextView.setText(context.getResources().getString(R.string.portfolio_header_stocks));
        }
    }
    public class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private Context context;

        public TextView stockSymbolAndExchangeTextView;
        public TextView timeTextView;
        public ImageView changeIconImageView;
        public TextView priceTextView;
        public TextView changeAbsTextView;
        public TextView changePercentTextView;

        public StockViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            stockSymbolAndExchangeTextView = (TextView) itemView.findViewById(R.id.list_item_market_symbol_exchange);
            timeTextView = (TextView) itemView.findViewById(R.id.list_item_market_time);
            changeIconImageView = (ImageView) itemView.findViewById(R.id.list_item_market_change_icon);
            priceTextView = (TextView) itemView.findViewById(R.id.list_item_market_price);
            changeAbsTextView = (TextView) itemView.findViewById(R.id.list_item_market_change_absolute);
            changePercentTextView = (TextView) itemView.findViewById(R.id.list_item_market_change_percent);

            itemView.setOnClickListener(this);
        }

        public void bindModel(Stock stock) {

            String stockSymbolAndExchangeStr = stock.getSymbol() + " (" + stock.getStockExchange() + ")";
            stockSymbolAndExchangeTextView.setText(stockSymbolAndExchangeStr);

            String time = MiscUtils.formatTimeOnly(stock.getQuote().getLastTradeTime().getTime());
            timeTextView.setText(time);

            BigDecimal price = stock.getQuote().getPrice();
            priceTextView.setText(price.toString());

            BigDecimal changeAbs = stock.getQuote().getChange();
            changeAbsTextView.setText(MiscUtils.formatChanges(changeAbs, false));
            BigDecimal changePercent = stock.getQuote().getChangeInPercent();
            changePercentTextView.setText(MiscUtils.formatChanges(changePercent, true));

            if (MiscUtils.isNonNegative(changeAbs)) {
                changeIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_change_history_green_24px));
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.accent));
                changePercentTextView.setTextColor(context.getResources().getColor(R.color.accent));
            } else {
                changeIconImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_change_history_red_24dp));
                changeAbsTextView.setTextColor(context.getResources().getColor(R.color.red));
                changePercentTextView.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        @Override
        public void onClick(View itemView) {
            Intent detailIntent = new Intent(getActivity(), StockDetailActivity.class);
            int index = getAdapterPosition() - ADDITIONAL_POSITIONS;
            detailIntent.putExtra(MarketFragment.SYMBOL, mStocks.get(index).getSymbol());
            startActivity(detailIntent);
            }
        }

    // --------------------- async task ------------------------------

    private class FetchStocksTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                mStocks = new ArrayList<>(YahooFinance.get(params).values());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setupAdapter();
        }
    }
}











