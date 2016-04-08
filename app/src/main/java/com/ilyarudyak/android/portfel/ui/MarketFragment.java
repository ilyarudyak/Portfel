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
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
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

    private List<Stock> mIndicesAndStocks;

    // used to build S&P500 chart
    private static Stock mIndexSnP500;

    private static final int POSITION_CHART = 0;
    private static final int POSITION_HEADER_INDICES = 1;
    private static final int INDEX_POSITION_OFFSET = 2;
    private static final int STOCK_POSITION_OFFSET = 3;

    private int mNumberOfIndices;
    private int mPositionHeaderStock;


    public static MarketFragment newInstance() {
        return new MarketFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // we store symbols to show in the fragment in shared prefs and use them to fetch info from yahoo
        mNumberOfIndices  = PrefUtils.getSymbols(getActivity(), getActivity()
                .getString(R.string.pref_market_symbols_indices)).size();
        mPositionHeaderStock = POSITION_HEADER_INDICES + mNumberOfIndices + 1;

        // we start service manually instead of using alarm - see comments in service
        if (savedInstanceState == null) {
            Log.d(TAG, "fetching data from onCreate() ... ");
            fetchDataWithService();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        setupRecyclerView();

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchDataWithService();
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
        if (isAdded()) {
            if (mIndicesAndStocks != null && mIndicesAndStocks.size() > 0) {
                mRecyclerView.setAdapter(new MarketDataAdapter());
            }
        }
    }
    private void fetchDataWithService() {
        getActivity().startService(MarketUpdateService.newIntent(getActivity()));
    }

    // ------------------- RecyclerView classes -----------------

    private class MarketDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
                stock = mIndicesAndStocks.get(position - STOCK_POSITION_OFFSET);
                ((StockViewHolder) holder).bindModel(stock);
            }
        }

        @Override
        public int getItemCount() {
            return mIndicesAndStocks.size() + STOCK_POSITION_OFFSET;
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
    public class ChartViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private LineChart indexLineChart;

        public ChartViewHolder(Context context, View view) {
            super(view);
            this.context = context;
            indexLineChart = (LineChart) view.findViewById(R.id.market_list_item_line_chart);
        }

        public void bindModel() {
            if (mIndexSnP500 == null) {
                new FetchIndexHistory(context, indexLineChart).execute();
            } else {
                try {
                    ChartUtils.buildLineChart(context, indexLineChart, mIndexSnP500);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public class HeaderViewHolder extends RecyclerView.ViewHolder {

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
            implements View.OnClickListener, View.OnLongClickListener {

        private Context context;

        public TextView symbolExchangeTextView;
        public TextView timeTextView;
        public ImageView changeIconImageView;
        public TextView priceTextView;
        public TextView changeAbsTextView;
        public TextView changePercentTextView;

        public StockViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
            symbolExchangeTextView = (TextView) itemView.findViewById(R.id.list_item_market_symbol_exchange);
            timeTextView = (TextView) itemView.findViewById(R.id.list_item_market_time);
            changeIconImageView = (ImageView) itemView.findViewById(R.id.list_item_market_change_icon);
            priceTextView = (TextView) itemView.findViewById(R.id.list_item_market_price);
            changeAbsTextView = (TextView) itemView.findViewById(R.id.list_item_market_change_absolute);
            changePercentTextView = (TextView) itemView.findViewById(R.id.list_item_market_change_percent);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
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
                // if we don't have predefined name just use symbol
                return stock.getSymbol();
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
            symbolExchangeTextView.setText(symbol);

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
            int index = getIndex(getAdapterPosition());
            if (index != -1) {
                detailIntent.putExtra(SYMBOL, mIndicesAndStocks.get(index).getSymbol());
                startActivity(detailIntent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            makeSnackBar(context, v, getAdapterPosition());
            return true;
        }
    }

    // helper methods
    private void makeSnackBar(final Context context, View v, final int adapterPosition) {
        String textStr = context.getString(R.string.market_snackbar_text);
        String actionStr = context.getString(R.string.market_snackbar_action);
        final String symbolStr = mIndicesAndStocks.get(getIndex(adapterPosition)).getSymbol();
        Snackbar snackbar = Snackbar.make(v, textStr, Snackbar.LENGTH_LONG)
                .setAction(actionStr, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String deleteTextStr;
                        if (deleteSymbol(context, symbolStr, adapterPosition) > 0) {
                            deleteTextStr = String.format(context.getString(
                                    R.string.market_snackbar_text_deleted), symbolStr);
                        } else {
                            deleteTextStr = String.format(context.getString(
                                    R.string.market_snackbar_text_deleted_error), symbolStr);
                        }
                        Snackbar.make(v, deleteTextStr, Snackbar.LENGTH_SHORT).show();
                    }
                });
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.primary));
        snackbar.show();
    }
    private int deleteSymbol(Context context, String symbolStr, int adapterPosition) {
        if (POSITION_HEADER_INDICES < adapterPosition && adapterPosition < mPositionHeaderStock) {
            // remove indices from shared prefs file
            String indices = context.getString(R.string.pref_market_symbols_indices);
            Log.d(TAG, PrefUtils.getSymbols(context, indices).toString());
            PrefUtils.removeSymbol(context, indices, symbolStr);
            Log.d(TAG, PrefUtils.getSymbols(context, indices).toString());

            // correct number of indices and position os stocks header
            mNumberOfIndices--;
            mPositionHeaderStock--;
        } else {
            // remove stocks from shared prefs file
            String stocks = context.getString(R.string.pref_market_symbols_stocks);
            Log.d(TAG, PrefUtils.getSymbols(context, stocks).toString());
            PrefUtils.removeSymbol(context, stocks, symbolStr);
            Log.d(TAG, PrefUtils.getSymbols(context, stocks).toString());
        }
        // we have only one table in DB: both for indices and stocks
        return context.getContentResolver().delete(PortfolioContract.StockTable.CONTENT_URI,
                PortfolioContract.StockTable.SYMBOL + " = '" + symbolStr + "'", null);
    }
    private int getIndex(int adapterPosition) {
        if (POSITION_HEADER_INDICES < adapterPosition && adapterPosition < mPositionHeaderStock) {
            return adapterPosition - INDEX_POSITION_OFFSET;
        } else if (adapterPosition > mPositionHeaderStock) {
            return adapterPosition - STOCK_POSITION_OFFSET;
        } else {
            return -1;
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

                // reverse history
                MiscUtils.reverseHistory(mIndexSnP500);

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
        if (cursor != null && cursor.getCount() > 0) {
            mIndicesAndStocks = DataUtils.buildStockList(cursor);
            setupAdapter();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

}