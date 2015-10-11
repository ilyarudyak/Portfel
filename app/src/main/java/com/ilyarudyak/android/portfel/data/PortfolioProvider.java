package com.ilyarudyak.android.portfel.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by ilyarudyak on 9/24/15.
 */
public class PortfolioProvider extends ContentProvider {

    public static final String TAG = PortfolioProvider.class.getSimpleName();

    private static final int STOCK = 100;
    private static final int STOCK_ID = 101;

    private PortfolioDbHelper mPortfolioDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PortfolioContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PortfolioContract.PATH_STOCK, STOCK);
        matcher.addURI(authority, PortfolioContract.PATH_STOCK + "/*", STOCK_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mPortfolioDbHelper = new PortfolioDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c;
        final SQLiteDatabase db = mPortfolioDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case STOCK:
                c = db.query(PortfolioContract.StockTable.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case STOCK_ID:
                String stockId = PortfolioContract.StockTable.getStockId(uri);
                c = db.query(PortfolioContract.StockTable.TABLE_NAME, projection,
                        BaseColumns._ID + "=" + stockId,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            c.setNotificationUri(context.getContentResolver(), uri);
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case STOCK:
                return PortfolioContract.StockTable.CONTENT_TYPE;
            case STOCK_ID:
                return PortfolioContract.StockTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mPortfolioDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch(match) {
            case STOCK: {
                long stockId = db.insertOrThrow(PortfolioContract.StockTable.TABLE_NAME, null, values);
                returnUri = PortfolioContract.StockTable.buildStockUri(stockId);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mPortfolioDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        String id;
        String selectionCriteria;
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (selection == null) selection = "1";

        switch(match) {
            case STOCK:
                return db.delete(PortfolioContract.StockTable.TABLE_NAME, selection, selectionArgs);
            case STOCK_ID:
                id = PortfolioContract.StockTable.getStockId(uri);
                selectionCriteria = BaseColumns._ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

                rowsDeleted = db.delete(PortfolioContract.StockTable.TABLE_NAME, selectionCriteria, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        if (rowsDeleted != 0) {
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}
