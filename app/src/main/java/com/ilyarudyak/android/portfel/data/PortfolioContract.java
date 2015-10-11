package com.ilyarudyak.android.portfel.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ilyarudyak on 9/24/15.
 */
public class PortfolioContract {

    public static final String CONTENT_AUTHORITY = "com.ilyarudyak.android.portfel";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_STOCK = "stock";

    public static final class StockTable implements BaseColumns {

        public static final String TABLE_NAME = "stock";

        public static final String SYMBOL = "symbol";
        public static final String NAME = "name";
        public static final String CURRENCY = "currency";
        public static final String STOCK_EXCHANGE = "exchange";
        public static final String PRICE = "price";
        public static final String PREVIOUS_CLOSE = "previous_close";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_STOCK)
                .build();

        public static final String CONTENT_TYPE = ContentResolver
                .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;
        public static final String CONTENT_ITEM_TYPE = ContentResolver
                .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        // (3) build uri based on id and return id based on uri
        public static Uri buildStockUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static String getStockId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

}
