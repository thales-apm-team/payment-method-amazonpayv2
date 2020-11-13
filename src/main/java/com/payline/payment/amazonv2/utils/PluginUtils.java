package com.payline.payment.amazonv2.utils;

import com.payline.pmapi.bean.common.Amount;

public class PluginUtils {

    private PluginUtils() {
        // ras.
    }

    public static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
        }
        return value;
    }


    /**
     * Check if a String is null, empty or filled with space
     *
     * @param value the String to check
     * @return true if the string is empty
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Return a string which was converted from cents to currency units
     *
     * @param amount the amount to convert
     * @return a String of the converted amount for example
     */
    public static String createStringAmount(Amount amount) {
        StringBuilder sb = new StringBuilder();
        sb.append(amount.getAmountInSmallestUnit());

        // get digit number of the currency and add a dot in the right place
        int nbDigits = amount.getCurrency().getDefaultFractionDigits();
        for (int i = sb.length(); i < nbDigits + 1; i++) {
            sb.insert(0, "0");
        }
        sb.insert(sb.length() - nbDigits, ".");

        return sb.toString();
    }

    /**
     * Return a string which was converted from cents to currency units and add the device symbol right after it
     * @param amount the amount to convert
     * @return the string to show
     */
    public static String createStringAmountToShow(Amount amount) {
        return createStringAmount(amount) + amount.getCurrency().getSymbol();
    }

    public static String addIfExist(String s) {
        String toReturn = "";
        if (!isEmpty(s)) {
            toReturn = " " + s;
        }
        return toReturn;
    }
}