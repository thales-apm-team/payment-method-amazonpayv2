package com.payline.payment.amazonv2.bean;

import com.payline.payment.amazonv2.bean.nested.*;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CheckoutSession extends AmazonBean {
    String checkoutSessionId;
    ChargePermissionType chargePermissionType;
    RecurringMetaData recurringMetaData;
    WebCheckoutDetails webCheckoutDetails;
    String productType;
    PaymentDetails paymentDetails;
    MerchantMetadata merchantMetadata;
    String platformId;
    ProviderMetadata providerMetadata;
    Buyer buyer;
    Address shippingAddress;
    Address billingAddress;
    List<PaymentPreference> paymentPreferences;
    StatusDetails statusDetails;
    List<Constraint> constraints;
    Date expirationTimestamp;
    String chargePermissionId;
    String chargeId;
    String storeId;
    DeliverySpecifications deliverySpecifications;

}
