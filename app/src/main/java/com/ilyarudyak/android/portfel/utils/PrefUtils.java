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
    public static final String PREFS_NAME = "symbols";
    public static final String INDICES = "indices";
    public static final String STOCKS = "stocks";

    public static Set<String> getSymbols(Context c, String symbolType) {
        String[] defaultSymbols;
        switch (symbolType) {
            case INDICES:
                defaultSymbols = c.getResources().getStringArray(R.array.index_symbols_default);
                break;
            case STOCKS:
                defaultSymbols = c.getResources().getStringArray(R.array.stock_symbols_default);
                break;
            default:
                throw new IllegalArgumentException("unknown symbol");
        }

        return c.getSharedPreferences(PREFS_NAME, 0)
                .getStringSet(symbolType, new HashSet<>(Arrays.asList(defaultSymbols)));
    }
    public static void putSymbol(Context c, String symbolType, String symbol) {

        Set<String> symbols = getSymbols(c, symbolType);
        if (symbols == null) {
            symbols = new HashSet<>();
        }
        symbols.add(symbol);

        // put updated set back into prefs file
        SharedPreferences.Editor editor = c.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putStringSet(symbolType, symbols).apply();
    }
    public static void removeSymbol(Context c, String symbolType, String symbol) {

        Set<String> symbols = getSymbols(c, symbolType);
        symbols.remove(symbol);

        // put updated set back into prefs file
        SharedPreferences.Editor editor = c.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putStringSet(symbolType, symbols).apply();
    }

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
