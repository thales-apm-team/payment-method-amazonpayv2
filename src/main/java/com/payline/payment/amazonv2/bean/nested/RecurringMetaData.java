package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class RecurringMetaData {
    private Frequency frequency;
    private Price amount;
}
