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
    String merchantId;
    @NonNull
    String ledgerCurrency;
    boolean sandbox;
    String checkoutLanguage;
    ProductType productType;    // default is PayAndShip
    @NonNull
    Placement placement;
    ButtonColor buttonColor;    // default is Gold
    @NonNull
    CreateCheckoutSessionConfig createCheckoutSessionConfig;

}
