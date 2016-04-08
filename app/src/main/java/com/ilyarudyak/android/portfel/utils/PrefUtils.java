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

    public static void setDefaultSymbols(Context context) {

        SharedPreferences.Editor symbolsEditor = context.getSharedPreferences(PREF_MARKET_SYMBOLS,
                Context.MODE_PRIVATE).edit();

        Set<String> defaultIndices = new HashSet<>(Arrays.asList(context.getResources()
                .getStringArray(R.array.index_symbols_default)));
        symbolsEditor.putStringSet(context.getString(R.string.pref_market_symbols_indices),
                defaultIndices).apply();

        Set<String> defaultStocks = new HashSet<>(Arrays.asList(context.getResources()
                .getStringArray(R.array.stock_symbols_default)));
        symbolsEditor.putStringSet(context.getString(R.string.pref_market_symbols_stocks),
                defaultStocks).apply();

    }
    public static Set<String> getSymbols(Context context, String symbolType) {
        String[] defaultSymbols;
        if (symbolType.equals(context.getString(R.string.pref_market_symbols_indices))) {
            defaultSymbols = context.getResources().getStringArray(R.array.index_symbols_default);
        } else {
            defaultSymbols = context.getResources().getStringArray(R.array.stock_symbols_default);
        }
        return context.getSharedPreferences(PREF_MARKET_SYMBOLS, Context.MODE_PRIVATE)
                .getStringSet(symbolType, new HashSet<>(Arrays.asList(defaultSymbols)));
    }
    public static void putSymbol(Context context, String symbolType, String symbol) {
        Set<String> symbols = getSymbols(context, symbolType);
        symbols.add(symbol);
        SharedPreferences.Editor symbolsEditor = context.getSharedPreferences(PREF_MARKET_SYMBOLS,
                Context.MODE_PRIVATE).edit();
        symbolsEditor.putStringSet(symbolType, symbols).apply();
    }
    public static void removeSymbol(Context context, String symbolType, String symbol) {
        Set<String> symbols = getSymbols(context, symbolType);
        symbols.remove(symbol);
        SharedPreferences.Editor symbolsEditor = context.getSharedPreferences(PREF_MARKET_SYMBOLS,
                Context.MODE_PRIVATE).edit();
        symbolsEditor.putStringSet(symbolType, symbols).apply();
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










