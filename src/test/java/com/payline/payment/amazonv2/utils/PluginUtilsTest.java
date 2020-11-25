package com.payline.payment.amazonv2.utils;


import com.payline.pmapi.bean.common.Amount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Currency;

class PluginUtilsTest {

    @Test
    void truncate() {
        Assertions.assertEquals(null, PluginUtils.truncate(null, 10));
        Assertions.assertEquals("", PluginUtils.truncate("message", 0));
        Assertions.assertEquals("this is a ", PluginUtils.truncate("this is a long message", 10));
        Assertions.assertEquals("foo", PluginUtils.truncate("foo", 10));
    }

    @Test
    void isEmpty() {
        Assertions.assertTrue(PluginUtils.isEmpty(null));
        Assertions.assertTrue(PluginUtils.isEmpty(""));
        Assertions.assertTrue(PluginUtils.isEmpty(" "));
        Assertions.assertFalse(PluginUtils.isEmpty("foo"));
    }

    @Test
    void testCreateStringAmount() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        Assertions.assertEquals("0.00", PluginUtils.createStringAmount(new Amount(int1, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.01", PluginUtils.createStringAmount(new Amount(int2, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.10", PluginUtils.createStringAmount(new Amount(int3, Currency.getInstance("EUR"))));
        Assertions.assertEquals("1.00", PluginUtils.createStringAmount(new Amount(int4, Currency.getInstance("EUR"))));
        Assertions.assertEquals("10.00", PluginUtils.createStringAmount(new Amount(int5, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.000", PluginUtils.createStringAmount(new Amount(int1, Currency.getInstance("KWD"))));
        Assertions.assertEquals("0.001", PluginUtils.createStringAmount(new Amount(int2, Currency.getInstance("KWD"))));
        Assertions.assertEquals("0.010", PluginUtils.createStringAmount(new Amount(int3, Currency.getInstance("KWD"))));
        Assertions.assertEquals("0.100", PluginUtils.createStringAmount(new Amount(int4, Currency.getInstance("KWD"))));
        Assertions.assertEquals("1.000", PluginUtils.createStringAmount(new Amount(int5, Currency.getInstance("KWD"))));
    }

    @Test
    void testCreateStringAmountToShow() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        Assertions.assertEquals("0.00€", PluginUtils.createStringAmountToShow(new Amount(int1, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.01€", PluginUtils.createStringAmountToShow(new Amount(int2, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.10€", PluginUtils.createStringAmountToShow(new Amount(int3, Currency.getInstance("EUR"))));
        Assertions.assertEquals("1.00€", PluginUtils.createStringAmountToShow(new Amount(int4, Currency.getInstance("EUR"))));
        Assertions.assertEquals("10.00€", PluginUtils.createStringAmountToShow(new Amount(int5, Currency.getInstance("EUR"))));
        Assertions.assertEquals("0.000KWD", PluginUtils.createStringAmountToShow(new Amount(int1, Currency.getInstance("KWD"))));
        Assertions.assertEquals("0.001KWD", PluginUtils.createStringAmountToShow(new Amount(int2, Currency.getInstance("KWD"))));
        Assertions.assertEquals("0.010KWD", PluginUtils.createStringAmountToShow(new Amount(int3, Currency.getInstance("KWD"))));
        Assertions.assertEquals("0.100KWD", PluginUtils.createStringAmountToShow(new Amount(int4, Currency.getInstance("KWD"))));
        Assertions.assertEquals("1.000KWD", PluginUtils.createStringAmountToShow(new Amount(int5, Currency.getInstance("KWD"))));
    }

    @Test
    void addIfExist() {
        Assertions.assertEquals("", PluginUtils.addIfExist(null));
        Assertions.assertEquals("", PluginUtils.addIfExist(""));
        Assertions.assertEquals(" foo", PluginUtils.addIfExist("foo"));
    }
}