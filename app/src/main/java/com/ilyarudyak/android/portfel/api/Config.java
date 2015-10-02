package com.ilyarudyak.android.portfel.api;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ilyarudyak on 9/24/15.
 */
public class Config {

    public static final URL NEWS_URL;
    public static final URL S_AND_P_URL;

    static {
        URL urlNews = null;
        URL urlSP = null;
        try {
//            url = new URL("http://www.forbes.com/markets/index.xml" );
            urlNews = new URL("http://rss.news.yahoo.com/rss/mostviewed");
            urlSP = new URL("http://chart.finance.yahoo.com/z?s=%5EGSPC&t=1m&q=l&l=off&z=m");
        } catch (MalformedURLException ignored) {
            // TODO: throw a real error
        }

        NEWS_URL = urlNews;
        S_AND_P_URL = urlSP;
    }
}
