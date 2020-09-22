package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class PaymentDetails {
    private PaymentIntent paymentIntent;
    private Boolean canHandlePendingAuthorization;
    private Price chargeAmount;
    private Price totalOrderAmount;
    private String presentmentCurrency;
    private String softDescriptor;
    private Boolean allowOvercharge;
    private Boolean extendExpiration;

    public enum PaymentIntent {
        Confirm, Authorize, AuthorizeWithCapture
    }
}
