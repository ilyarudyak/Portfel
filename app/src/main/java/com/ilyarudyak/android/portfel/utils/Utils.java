package com.ilyarudyak.android.portfel.utils;

import java.math.BigDecimal;

/**
 * Created by ilyarudyak on 10/1/15.
 */
public class Utils {

    public static boolean isNonNegative(BigDecimal number) {
        return number.compareTo(new BigDecimal(0)) >= 0;
    }

    public static String formatChanges(BigDecimal change, boolean isPercent) {

        String result;
        if (isNonNegative(change)) {
            result = "+" + change;
        } else {
            result = "-" + change;
        }

        if (isPercent) {
            result = result + "%";
        }

        return result;
    }
}
