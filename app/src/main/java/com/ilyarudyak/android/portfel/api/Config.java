package com.ilyarudyak.android.portfel.api;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ilyarudyak on 9/24/15.
 */
public class Config {

    public static final URL NEWS_URL;

    static {
        URL url = null;
        try {
//            url = new URL("http://www.forbes.com/markets/index.xml" );
            url = new URL("http://rss.news.yahoo.com/rss/mostviewed");
        } catch (MalformedURLException ignored) {
            // TODO: throw a real error
        }

        NEWS_URL = url;
    }
}
