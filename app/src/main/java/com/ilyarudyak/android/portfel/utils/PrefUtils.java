package com.ilyarudyak.android.portfel.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ilyarudyak.android.portfel.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ilyarudyak on 10/9/15.
 */
public class PrefUtils {

    private static final String TAG = PrefUtils.class.getSimpleName();

    // name of shared prefs file, that contains stocks
    // and indices symbols for market fragment
    public static final String PREF_MARKET_SYMBOLS = "com.ilyarudyak.android.portfel.MARKET_SYMBOLS";
    public static final String PREF_PORTFOLIO_STOCKS = "com.ilyarudyak.android.portfel.PORTFOLIO_STOCKS";

    // --------- indices and stocks from market fragment ----------

    public static Set<String> getSymbols(Context context, String symbolType) {
        String[] defaultSymbols;
        if (symbolType.equals(context.getString(R.string.pref_market_symbols_indices))) {
            defaultSymbols = context.getResources().getStringArray(R.array.index_symbols_default);
        } else {
            defaultSymbols = context.getResources().getStringArray(R.array.stock_symbols_default);
        }
        return context.getSharedPreferences(PREF_MARKET_SYMBOLS, 0)
                .getStringSet(symbolType, new HashSet<>(Arrays.asList(defaultSymbols)));
    }
    public static void putSymbol(Context context, String symbolType, String symbol) {

        Set<String> symbols = getSymbols(context, symbolType);
        if (symbols == null) {
            symbols = new HashSet<>();
        }
        symbols.add(symbol);

        // put updated set back into prefs file
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_MARKET_SYMBOLS, 0).edit();
        editor.putStringSet(symbolType, symbols).apply();
    }
    public static void removeSymbol(Context context, String symbolType, String symbol) {

        Set<String> symbols = getSymbols(context, symbolType);
        symbols.remove(symbol);

        // put updated set back into prefs file
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_MARKET_SYMBOLS, 0).edit();
        editor.putStringSet(symbolType, symbols).apply();
    }
    public static boolean isIndex(Context context, String symbol) {
        Set<String> indices = getSymbols(context, context.getString(R.string.pref_market_symbols_indices));
        return indices.contains(symbol);
    }

    // --------- stocks from portfolio fragment -------------------

    public static Set<String> getPortfolioStocks(Context context) {
        String[] defaultPortfolioStocks = context.getResources()
                .getStringArray(R.array.stock_symbols_default);
        return context.getSharedPreferences(PREF_PORTFOLIO_STOCKS, Context.MODE_PRIVATE)
                      .getStringSet(context.getString(R.string.pref_market_symbols_stocks),
                              new HashSet<>(Arrays.asList(defaultPortfolioStocks)));
    }
    public static void putPortfolioStock(Context context, String stockSymbol) {

        Set<String> stockSet = getPortfolioStocks(context);
        if (stockSet == null) {
            stockSet = new HashSet<>();
        }
        stockSet.add(stockSymbol);

        // put updated set back into prefs file
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREF_MARKET_SYMBOLS, Context.MODE_PRIVATE).edit();
        editor.putStringSet(context.getString(R.string.pref_market_symbols_stocks), stockSet).apply();
    }
    public static void removePortfolioStock(Context context, String stockSymbol) {

        Set<String> stockSet = getPortfolioStocks(context);
        stockSet.remove(stockSymbol);

        // put updated set back into prefs file
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREF_MARKET_SYMBOLS, Context.MODE_PRIVATE).edit();
        editor.putStringSet(context.getString(R.string.pref_market_symbols_stocks), stockSet).apply();
    }

    // ---------------------- misc methods ------------------------

    public static String[] toArray(Set<String> set) {
        return set.toArray(new String[set.size()]);
    }
    public static String[] concat(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c= new String[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}










