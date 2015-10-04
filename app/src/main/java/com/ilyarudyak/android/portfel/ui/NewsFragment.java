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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.zip.DataFormatException;

public class NewsFragment extends Fragment {

    public static final String TAG = NewsFragment.class.getSimpleName();
    private static final String KEY_POSITION = "com.ilyarudyak.android.portfel.ui.POSITION";

    private TextView mTextView;
    private RecyclerView mRecyclerView;
    private Feed mFeed;

    public static NewsFragment newInstance(int position) {

        NewsFragment pf = new NewsFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        pf.setArguments(args);

        return pf;
    }

    @Override
    public void onStart() {
        super.onStart();
        new FetchNewsFeed().execute(Config.NEWS_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.news_recycler_view);

        return view;
    }

    // helper methods
    private void setRecyclerView(Feed feed) {

        // set layout manager
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(llm);

        // set divider
        Drawable divider = getResources().getDrawable(R.drawable.padded_divider);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration(divider));

        // set adapter
        NewsFeedAdapter articleAdapter = new NewsFeedAdapter();
        mRecyclerView.setAdapter(articleAdapter);
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
            if (mFeed != null) {
                setRecyclerView(mFeed);
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
            holder.titleTextView.setText(title);

//            Log.d(TAG, item.getImageLink());
            String urlStr = item.getImageLink();
            if (urlStr != null) {
                Picasso.with(getActivity())
                        .load(item.getImageLink())
                        .into(holder.imageView);
            }

            Date date = item.getPublicationDate();
            if (date != null) {
                holder.dateTextView.setText(date.toString());
            }
        }

        @Override
        public int getItemCount() {
            return mFeed.getItems().size();
        }
    }
    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public ImageView imageView;
        public TextView titleTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.list_item_news_image_view);
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














