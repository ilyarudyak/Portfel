package com.ilyarudyak.android.portfel.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.ilyarudyak.android.portfel.R;

public class NewsDetailActivity extends Activity {

    public static final String EXTRA_RSS_ITEM_URL_STRING =
            "com.ilyarudyak.android.portfel.ui.EXTRA_RSS_ITEM_URL_STRING";

    private WebView mBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        mBrowser = (WebView) findViewById(R.id.webkit);

        setWebView();
    }

    private void setWebView() {
        Intent intent = getIntent();
        String newsUrlStr = intent.getStringExtra(EXTRA_RSS_ITEM_URL_STRING);
        mBrowser.loadUrl(newsUrlStr);
    }
}
