package com.ilyarudyak.android.portfel.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ilyarudyak on 9/24/15.
 */
public class PortfolioDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "portfolio.db";

    public PortfolioDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + PortfolioContract.StockTable.TABLE_NAME + " (" +
                PortfolioContract.StockTable._ID +                  " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PortfolioContract.StockTable.SYMBOL +               " INTEGER UNIQUE NOT NULL, " +
                PortfolioContract.StockTable.NAME +                 " TEXT NOT NULL, " +
                PortfolioContract.StockTable.CURRENCY +             " TEXT NOT NULL, " +
                PortfolioContract.StockTable.STOCK_EXCHANGE +       " TEXT NOT NULL, " +
                PortfolioContract.StockTable.PRICE +                " TEXT NOT NULL, " +
                PortfolioContract.StockTable.PREVIOUS_CLOSE +       " TEXT NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_STOCK_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PortfolioContract.StockTable.TABLE_NAME);
        onCreate(db);
    }
}
