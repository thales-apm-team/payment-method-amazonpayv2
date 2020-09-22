package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class Price {
    private String amount;
    private String currencyCode;
}
