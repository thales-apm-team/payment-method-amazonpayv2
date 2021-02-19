package com.payline.payment.amazonv2.bean;

import com.payline.payment.amazonv2.bean.nested.MerchantMetadata;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.ProviderMetadata;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Charge extends AmazonBean  {
    String chargeId;
    String chargePermissionId;
    Price chargeAmount;
    Price captureAmount;
    Price refundedAmount;
    String convertedAmount;
    String conversionRate;
    String softDescriptor;
    MerchantMetadata merchantMetadata;
    ProviderMetadata providerMetadata;
    StatusDetails statusDetails;
}
