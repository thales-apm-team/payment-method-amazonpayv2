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
    private String checkoutSessionId;
    private ChargePermissionType chargePermissionType;
    private RecurringMetaData recurringMetaData;
    private WebCheckoutDetails webCheckoutDetails;
    private String productType;
    private PaymentDetails paymentDetails;
    private MerchantMetadata merchantMetadata;
    private String platformId;
    private ProviderMetadata providerMetadata;
    private Buyer buyer;
    private Address shippingAddress;
    private Address billingAddress;
    private List<PaymentPreference> paymentPreferences;
    private StatusDetails statusDetails;
    private List<Constraint> constraints;
    private Date expirationTimestamp;
    private String chargePermissionId;
    private String chargeId;
    private String storeId;
    private DeliverySpecifications deliverySpecifications;

}
