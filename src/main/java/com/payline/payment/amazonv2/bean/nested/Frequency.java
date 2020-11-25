package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class Frequency {
    private Unit unit;
    private String value;

    public enum Unit{
        Year, Month, Week, Day, Variable
    }
}
