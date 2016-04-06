package com.ilyarudyak.android.portfel.api;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ilyarudyak on 9/24/15.
 */
public class Config {

    public static final URL NEWS_URL;
    public static final URL SP500_URL;
    public static final URL REUTERS_URL;

//    public static final String COMPANY_NEWS_BASE_URL = "http://finance.yahoo.com/rss/headline";
    public static final String COMPANY_NEWS_BASE_URL = "https://feeds.finance.yahoo.com/rss/2.0/headline";

    static {
        URL urlNews = null;
        URL urlSP = null;
        URL urlReuters = null;
        try {
//            url = new URL("http://www.forbes.com/markets/index.xml" );
            urlNews = new URL("http://rss.news.yahoo.com/rss/mostviewed");
            urlSP = new URL("http://chart.finance.yahoo.com/z?s=%5EGSPC&t=1m&q=l&l=off&z=m");
            urlReuters = new URL("http://feeds.reuters.com/reuters/businessNews");

        } catch (MalformedURLException ignored) {
            // TODO: throw a real error
        }

        NEWS_URL = urlNews;
        SP500_URL = urlSP;
        REUTERS_URL = urlReuters;
    }

    public static URL getCompanyNewsUrl(String symbol) throws MalformedURLException {
        Uri builtUri = Uri.parse(COMPANY_NEWS_BASE_URL)
                .buildUpon()
                .appendQueryParameter("s", symbol)
                .appendQueryParameter("region", "US")
                .appendQueryParameter("lang", "en-US")
                .build();
        return new URL(builtUri.toString());
    }
}
