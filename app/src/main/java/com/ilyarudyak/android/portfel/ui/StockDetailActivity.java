package com.ilyarudyak.android.portfel.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.settings.SettingsActivity;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.ChartUtils;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.ilyarudyak.android.portfel.utils.PrefUtils;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.zip.DataFormatException;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class StockDetailActivity extends AppCompatActivity {

    private static final String TAG = StockDetailActivity.class.getSimpleName();

    private static final int POSITION_CHART = 0;
    private static final int POSITION_HEADER_NEWS = 1;
    // 2 = 1 for chart + 1 for header
    private static final int ADDITIONAL_POSITIONS = 2;

    private RecyclerView mRecyclerView;

    // get from intent
    private String mSymbol;
    private boolean mIsIndex = false;

    // get from async tasks
    private Stock mStock;
    private Feed mFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        if (savedInstanceState == null) {
            new FetchNewsFeed().execute();
        }
    }

    // helper methods
    private void setViews() {
        setToolbar();
        setRecyclerView();
        setShareFab();
    }
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        if (mStock != null) {

            TextView name = (TextView) findViewById(R.id.activity_stock_detail_name);
            name.setText(mStock.getName());

            TextView subtitle = (TextView) findViewById(R.id.activity_stock_detail_subtitle);
            String subtitleStr = mStock.getSymbol() + " (" + mStock.getStockExchange() + ") " +
                    MiscUtils.formatTimeOnly(mStock.getQuote().getLastTradeTime().getTime());
            subtitle.setText(subtitleStr);

            TextView price = (TextView) findViewById(R.id.activity_stock_detail_price);
            price.setText(String.format(mStock.getQuote().getPrice().toString(), Locale.US));

            ImageView changeIcon = (ImageView) findViewById(R.id.activity_stock_detail_change_icon);
            TextView changeAbs = (TextView) findViewById(R.id.activity_stock_detail_change_abs);
            TextView changePercent = (TextView) findViewById(R.id.activity_stock_detail_change_percent);
            changeAbs.setText(MiscUtils.formatChangesDetails(mStock.getQuote().getChange(), false));
            changePercent.setText(MiscUtils.formatChangesDetails(mStock.getQuote().getChangeInPercent(), true));

            if (MiscUtils.isNonNegative(mStock.getQuote().getChange())) {
                changeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_change_history_green_24px));
                changeAbs.setTextColor(getResources().getColor(R.color.accent));
                changePercent.setTextColor(getResources().getColor(R.color.accent));
            } else {
                changeIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_change_history_red_24dp));
                changeAbs.setTextColor(getResources().getColor(R.color.red));
                changePercent.setTextColor(getResources().getColor(R.color.red));
            }


        }
    }
    private void setRecyclerView() {
        Log.d(TAG, "setting recycler view...");
        if (mStock != null) {
            // set layout manager
            LinearLayoutManager llm = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(llm);

            // set divider
            Drawable divider = getResources().getDrawable(R.drawable.padded_divider);
            mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration(divider));

            // set adapter
            StockDetailDataAdapter stockDetailDataAdapter = new StockDetailDataAdapter();
            mRecyclerView.setAdapter(stockDetailDataAdapter);
            Log.d(TAG, "setting recycler view DONE");
        }
    }
    private void setShareFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.stock_detail_fab_share);
        if (mStock != null) {
            final String message = mStock.getSymbol() + ":" + mStock.getStockExchange() + " " +
                    mStock.getQuote().getPrice() + " (" + MiscUtils.formatChanges(
                    mStock.getQuote().getChange(), false) + ")";
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    share(message);
                }
            });
        }
    }
    private void share(String message) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(i);
    }

    // ------------------ options menu -------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stock_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    // ----------------------- async tasks ---------------------------

    private class FetchStockHistory extends AsyncTask<Void, Void, Void> {

        private final String TAG = FetchStockHistory.class.getSimpleName();

        private Context context;
        private LineChart lineChart;
        private CandleStickChart candleStickChart;

        public FetchStockHistory(Context context, CandleStickChart candleStickChart,  LineChart lineChart) {
            this.candleStickChart = candleStickChart;
            this.context = context;
            this.lineChart = lineChart;
        }

        @Override
        protected Void doInBackground(Void... ignore) {
            // get info from intent
            mSymbol = getIntent().getStringExtra(MarketFragment.SYMBOL);
            Log.d(TAG, "symbol=" + mSymbol);
            if (PrefUtils.isIndex(StockDetailActivity.this, mSymbol)) {
                mIsIndex = true;
            }

            // fetch history
            try {
                mStock = YahooFinance.get(mSymbol, true);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void ignore) {

        }
    }
    private class FetchNewsFeed extends AsyncTask<URL, Void, Void> {

        @Override
        protected Void doInBackground(URL... urls) {

            InputStream inputStream;
            try {
                inputStream = urls[0].openConnection().getInputStream();
                mFeed = EarlParser.parseOrThrow(inputStream, 0);
            } catch (IOException | DataFormatException | XmlPullParserException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void ignore) {
            setViews();
        }
    }

    // ------------------- RecyclerView classes -----------------

    private class StockDetailDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            switch (viewType) {
                case R.id.view_holder_chart:
                    if (!mIsIndex) {
                        view = StockDetailActivity.this.getLayoutInflater().inflate(
                                R.layout.list_item_stock_detail_candlestick_chart, parent, false);

                    } else {
                        view = StockDetailActivity.this.getLayoutInflater().inflate(
                                R.layout.list_item_market_line_chart, parent, false);
                    }
                    return new ChartViewHolder(view);
                case R.id.view_holder_header:
                    view = StockDetailActivity.this.getLayoutInflater().inflate(
                            R.layout.list_item_market_header, parent, false);
                    return new HeaderViewHolder(view);
                default:
                    view = StockDetailActivity.this.getLayoutInflater().inflate(
                            R.layout.list_item_news, parent, false);
                    return new NewsViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (position == POSITION_CHART) {
                ((ChartViewHolder) holder).bindModel();
            } else if (position == POSITION_HEADER_NEWS) {
                ((HeaderViewHolder) holder).bindModel();
            } else {
                ((NewsViewHolder) holder).bindModel();
            }
        }

        @Override
        public int getItemCount() {
            return mFeed.getItems().size() + ADDITIONAL_POSITIONS;
        }

        @Override
        public int getItemViewType(int position) {

            if (position == POSITION_CHART) {
                return R.id.view_holder_chart;
            } else if (position == POSITION_HEADER_NEWS) {
                return R.id.view_holder_header;
            } else { // position >= 2
                return R.id.view_holder_news;
            }
        }
    }
    public class ChartViewHolder extends RecyclerView.ViewHolder {

        private CandleStickChart candleStickChart;
        private LineChart lineChart;

        public ChartViewHolder(View view) {
            super(view);
            if (!mIsIndex) {
                candleStickChart = (CandleStickChart) view.findViewById(
                        R.id.stock_detail_list_item_candlestick_chart);
            } else {
                lineChart = (LineChart) view.findViewById(R.id.market_list_item_line_chart);
            }
        }

        public void bindModel() {
            try {
                if (mStock != null) {
                    if (!mIsIndex) {
                        ChartUtils.buildCandleStickChart(StockDetailActivity.this,
                                candleStickChart, mStock);
                    } else {
                        ChartUtils.buildLineChart(StockDetailActivity.this, lineChart, mStock);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public TextView headerTextView;

        public HeaderViewHolder(View view) {
            super(view);
            headerTextView = (TextView) view.findViewById(R.id.market_list_item_header);
        }

        public void bindModel() {

            headerTextView.setText(StockDetailActivity.this.getResources()
                        .getString(R.string.stock_detail_header_news));
        }
    }
    public class NewsViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public ImageView thumbnailImageView;
        public ImageView clockImageView;
        public TextView titleTextView;
        public TextView dateTextView;

        public NewsViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.list_item_news_image_view);
            clockImageView = (ImageView) itemView.findViewById(R.id.list_item_news_clock_icon);
            titleTextView = (TextView) itemView.findViewById(R.id.list_item_news_title);
            dateTextView = (TextView) itemView.findViewById(R.id.list_item_news_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView) {
            Item rssItem = mFeed.getItems().get(getAdapterPosition() - ADDITIONAL_POSITIONS);
            String itemUrlStr = rssItem.getLink();
            Intent detailIntent = new Intent(StockDetailActivity.this, NewsDetailActivity.class);
            detailIntent.putExtra(NewsDetailActivity.EXTRA_RSS_ITEM_URL_STRING, itemUrlStr);
            startActivity(detailIntent);
        }

        public void bindModel() {
            if (mFeed != null) {
                Item item = mFeed.getItems().get(getAdapterPosition() - ADDITIONAL_POSITIONS);

                String title = item.getTitle();
                // if no specific for a company RSS feed found - replace error message with
                // something more appropriate for a user
                if (title != null && title.contains(StockDetailActivity.this.getString(
                        R.string.stock_detail_no_rss_error))) {
                    title = StockDetailActivity.this.getString(R.string.stock_detail_no_rss_handler);
                }
                titleTextView.setText(title);

                String urlStr = item.getImageLink();
                if (urlStr != null) {
                    Picasso.with(StockDetailActivity.this)
                            .load(urlStr)
                            .into(thumbnailImageView);
                } else {
                    thumbnailImageView.setVisibility(View.GONE);
                }

                Date date = item.getPublicationDate();
                if (date != null) {
                    String dateStr = MiscUtils.getTimeAgo(date.getTime());
                    if (dateStr == null) {
                        // we don't show clock icon if no time provided
                        clockImageView.setVisibility(View.GONE);
                        // we clear data - adapter can reuse an item with some string
                        dateTextView.setText("");
                    } else {
                        // we return icon - again adapter can reuse an item
                        clockImageView.setVisibility(View.VISIBLE);
                        dateTextView.setText(dateStr);
                    }
                } else {
                    clockImageView.setVisibility(View.GONE);
                }
            }
        }
    }
}













