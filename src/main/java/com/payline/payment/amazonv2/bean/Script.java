package com.payline.payment.amazonv2.bean;

import com.payline.payment.amazonv2.bean.nested.ButtonColor;
import com.payline.payment.amazonv2.bean.nested.CreateCheckoutSessionConfig;
import com.payline.payment.amazonv2.bean.nested.Placement;
import com.payline.payment.amazonv2.bean.nested.ProductType;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class Script {

    @NonNull
    private final String merchantId;
    @NonNull
    private final String ledgerCurrency;
    private final boolean sandbox;
    private final String checkoutLanguage;
    private final ProductType productType;    // default is PayAndShip
    @NonNull
    private final Placement placement;
    private final ButtonColor buttonColor;    // default is Gold
    @NonNull
    private final CreateCheckoutSessionConfig createCheckoutSessionConfig;

}
