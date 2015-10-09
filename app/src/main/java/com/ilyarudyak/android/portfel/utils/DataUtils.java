package com.ilyarudyak.android.portfel.utils;

import android.content.ContentValues;
import android.database.Cursor;

import com.ilyarudyak.android.portfel.data.PortfolioContract;

import java.util.ArrayList;
import java.util.List;

import yahoofinance.Stock;

/**
 * Created by ilyarudyak on 10/6/15.
 */
public class DataUtils {

    public static ContentValues buildContentValues(Stock s) {

        ContentValues cv = new ContentValues();

        cv.put(PortfolioContract.StockTable.SYMBOL, s.getSymbol());
        cv.put(PortfolioContract.StockTable.NAME, s.getName());
        cv.put(PortfolioContract.StockTable.CURRENCY, s.getCurrency());
        cv.put(PortfolioContract.StockTable.STOCK_EXCHANGE, s.getStockExchange());
//        cv.put(PortfolioContract.StockQuoteTable.PRICE, s.getQuote().getPrice().toString());

        return cv;
    }

    public static List<Stock> buildStockList(Cursor c) {

        List<Stock> stockList = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                String symbol = c.getString(c.getColumnIndex(PortfolioContract.StockTable.SYMBOL));
                Stock stock = new Stock(symbol);
                stock.setName(c.getString(c.getColumnIndex(PortfolioContract.StockTable.NAME)));
                stock.setCurrency(c.getString(c.getColumnIndex(PortfolioContract.StockTable.CURRENCY)));
                stock.setStockExchange(c.getString(c.getColumnIndex(PortfolioContract.StockTable.STOCK_EXCHANGE)));

//                StockQuote quote = new StockQuote(symbol);
//                quote.setPrice(new BigDecimal(c.getString(c.getColumnIndex(PortfolioContract.StockQuoteTable.PRICE))));
//                stock.setQuote(quote);
                stockList.add(stock);
            } while (c.moveToNext());
        }

//        c.close();
        return stockList;
    }
}
