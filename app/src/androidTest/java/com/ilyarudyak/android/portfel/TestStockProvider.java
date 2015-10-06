package com.ilyarudyak.android.portfel;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.ilyarudyak.android.portfel.data.PortfolioContract;

public class TestStockProvider extends AndroidTestCase {

    public static final String TAG = TestStockProvider.class.getSimpleName();

    @Override
    public void setUp() {

        mContext.getContentResolver().delete(
                PortfolioContract.StockTable.CONTENT_URI,
                null,
                null
        );
    }

    public void testInsert() throws Throwable {

        ContentValues cv = TestUtils.createTeslaContentValues();
        mContext.getContentResolver().insert(PortfolioContract.StockTable.CONTENT_URI, cv);

        // query db and check entry
        Cursor c = mContext.getContentResolver().query(
                PortfolioContract.StockTable.CONTENT_URI,
                null, null, null, null);
        assertEquals("problems from testInsert()", 1, c.getCount());

        c.moveToFirst();
        assertEquals("TSLA", c.getString(1));
        assertEquals("Tesla", c.getString(2));
        assertEquals("USD", c.getString(3));
        assertEquals("NSE", c.getString(4));
        c.close();
    }

    public void testDelete() throws Throwable {

        // insert entry to database
        ContentValues cv = TestUtils.createTeslaContentValues();
        Uri uri = mContext.getContentResolver().insert(PortfolioContract.StockTable.CONTENT_URI, cv);

        mContext.getContentResolver().delete(uri, null, null);

        // query db and check entry
        Cursor c = mContext.getContentResolver().query(
                PortfolioContract.StockTable.CONTENT_URI,
                null, null, null, null);
        assertEquals("problems from testDelete()", 0, c.getCount());
        c.close();

    }
}