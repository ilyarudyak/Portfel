package com.ilyarudyak.android.portfel.data;

import android.content.ContentProvider;
import android.content.ContentValues;
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
    private static final int STOCK_QUOTE = 200;
    private static final int STOCK_QUOTE_ID = 201;

    private PortfolioDbHelper mPortfolioDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PortfolioContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, PortfolioContract.PATH_STOCK, STOCK);
        matcher.addURI(authority, PortfolioContract.PATH_STOCK_QUOTE, STOCK_QUOTE);

        matcher.addURI(authority, PortfolioContract.PATH_STOCK + "/*", STOCK_ID);
        matcher.addURI(authority, PortfolioContract.PATH_STOCK_QUOTE + "/*", STOCK_QUOTE_ID);

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
            case STOCK_QUOTE:
                c = db.query(PortfolioContract.StockQuoteTable.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case STOCK_QUOTE_ID:
                String stockQuoteId = PortfolioContract.StockQuoteTable.getStockQuoteId(uri);
                c = db.query(PortfolioContract.StockQuoteTable.TABLE_NAME, projection,
                        BaseColumns._ID + "=" + stockQuoteId,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
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
            case STOCK_QUOTE:
                return PortfolioContract.StockQuoteTable.CONTENT_TYPE;
            case STOCK_QUOTE_ID:
                return PortfolioContract.StockQuoteTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mPortfolioDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match) {
            case STOCK:
                long movieId = db.insertOrThrow(PortfolioContract.StockTable.TABLE_NAME, null, values);
                return PortfolioContract.StockTable.buildStockUri(movieId);
            case STOCK_QUOTE:
                long trailerId = db.insertOrThrow(PortfolioContract.StockQuoteTable.TABLE_NAME, null, values);
                return PortfolioContract.StockQuoteTable.buildStockQuoteUri(trailerId);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mPortfolioDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        String id;
        String selectionCriteria;

        // this makes delete all rows return the number of rows deleted
        if (selection == null) selection = "1";

        switch(match) {
            case STOCK:
                return db.delete(PortfolioContract.StockTable.TABLE_NAME, selection, selectionArgs);
            case STOCK_ID:
                id = PortfolioContract.StockTable.getStockId(uri);
                selectionCriteria = BaseColumns._ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

                return db.delete(PortfolioContract.StockTable.TABLE_NAME, selectionCriteria, selectionArgs);
            case STOCK_QUOTE:
                return db.delete(PortfolioContract.StockQuoteTable.TABLE_NAME, selection, selectionArgs);
            case STOCK_QUOTE_ID:
                id = PortfolioContract.StockQuoteTable.getStockQuoteId(uri);
                selectionCriteria = BaseColumns._ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");

                return db.delete(PortfolioContract.StockQuoteTable.TABLE_NAME, selectionCriteria, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
