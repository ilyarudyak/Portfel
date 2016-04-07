package com.ilyarudyak.android.portfel.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ilyarudyak.android.portfel.R;

public class NewsDetailActivity extends Activity {

    public static final String EXTRA_RSS_ITEM_URL_STRING =
            "com.ilyarudyak.android.portfel.ui.EXTRA_RSS_ITEM_URL_STRING";
    private static final String TAG = NewsDetailActivity.class.getSimpleName();

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        mWebView = (WebView) findViewById(R.id.webkit);

        // open redirected links in web view (not in browser)
        mWebView.setWebViewClient(new WebViewClient());

        setWebView();
    }

    private void setWebView() {
        String newsUrlStr = getIntent().getStringExtra(EXTRA_RSS_ITEM_URL_STRING);
        Log.d(TAG, newsUrlStr);
        if (newsUrlStr != null) {
            mWebView.loadUrl(newsUrlStr);
        }
    }
}
