package com.ilyarudyak.android.portfel.ui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.Item;
import com.ilyarudyak.android.portfel.R;
import com.ilyarudyak.android.portfel.api.Config;
import com.ilyarudyak.android.portfel.ui.divider.HorizontalDividerItemDecoration;
import com.ilyarudyak.android.portfel.utils.MiscUtils;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.zip.DataFormatException;

public class NewsFragment extends Fragment {

    public static final String TAG = NewsFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Feed mFeed;

    // on a tablet we use cards and gridlayout for RecyclerView
    private int mColumnNumber;

    public static NewsFragment newInstance() {
        NewsFragment pf = new NewsFragment();
        return pf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            fetchDataWithAsyncTask();
        }

        // get column number
        mColumnNumber = getResources().getInteger(R.integer.column_number);
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
                fetchDataWithAsyncTask();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    // helper methods
    private void fetchDataWithAsyncTask() {
        Log.d(TAG, "fetching data...");
        new FetchNewsFeed().execute(Config.NEWS_URL);
    }
    private void setupRecyclerView() {
        if (mColumnNumber == 1) {
            // set layout manager
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(llm);

            // set divider
            Drawable divider = getResources().getDrawable(R.drawable.padded_divider);
            mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration(divider));
        } else {
            GridLayoutManager glm = new GridLayoutManager(getActivity(), mColumnNumber);
            mRecyclerView.setLayoutManager(glm);
        }
    }
    private void setupAdapter() {
        if (isAdded()) {
            mRecyclerView.setAdapter(new NewsFeedAdapter());
        }
    }


    // ------------------- AsyncTask class -----------------

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
            if (mFeed != null && mRecyclerView != null) {
                setupAdapter();
            }
        }
    }

    // ------------------- RecyclerView classes -----------------

    private class NewsFeedAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_news, parent, false);
            ViewHolder vh = new ViewHolder(view);

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Item item = mFeed.getItems().get(position);

            String title = item.getTitle();
            if (title != null) {
                holder.titleTextView.setText(title);
            }

            String urlStr = item.getImageLink();
//            Log.d(TAG, "title=" + title.substring(0, 10) + " urlStr=" + urlStr);
            if (urlStr != null) {
                holder.thumbnailImageView.setVisibility(View.VISIBLE);
                Picasso.with(getActivity())
                        .load(urlStr)
                        .into(holder.thumbnailImageView);
            } else {
                holder.thumbnailImageView.setVisibility(View.INVISIBLE);
            }

            Date date = item.getPublicationDate();
            if (date != null) {
                String dateStr = MiscUtils.getTimeAgo(date.getTime());
                if (dateStr == null) {
                    // we don't show clock icon if no time provided
                    holder.clockImageView.setVisibility(View.GONE);
                    // we clear data - adapter can reuse an item with some string
                    holder.dateTextView.setText("");
                } else {
                    // we return icon - again adapter can reuse an item
                    holder.clockImageView.setVisibility(View.VISIBLE);
                    holder.dateTextView.setText(dateStr);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mFeed.getItems().size();
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public ImageView thumbnailImageView;
        public ImageView clockImageView;
        public TextView titleTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            thumbnailImageView = (ImageView) itemView.findViewById(R.id.list_item_news_image_view);
            clockImageView = (ImageView) itemView.findViewById(R.id.list_item_news_clock_icon);
            titleTextView = (TextView) itemView.findViewById(R.id.list_item_news_title);
            dateTextView = (TextView) itemView.findViewById(R.id.list_item_news_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView) {
            Item rssItem = mFeed.getItems().get(getAdapterPosition());
            String itemUrlStr = rssItem.getLink();
            Intent detailIntent = new Intent(getActivity(), NewsDetailActivity.class);
            detailIntent.putExtra(NewsDetailActivity.EXTRA_RSS_ITEM_URL_STRING, itemUrlStr);
            startActivity(detailIntent);
        }
    }
}














