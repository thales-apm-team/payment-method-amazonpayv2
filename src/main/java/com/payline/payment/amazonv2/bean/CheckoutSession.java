package com.payline.payment.amazonv2.bean;

import com.payline.payment.amazonv2.bean.nested.*;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
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

    @Builder
    public CheckoutSession(Date creationTimestamp, ReleaseEnvironment releaseEnvironment, String checkoutSessionId, ChargePermissionType chargePermissionType, RecurringMetaData recurringMetaData, WebCheckoutDetails webCheckoutDetails, String productType, PaymentDetails paymentDetails, MerchantMetadata merchantMetaData, String platformId, ProviderMetadata providerMetadata, Buyer buyer, Address shippingAddress, Address billingAddress, List<PaymentPreference> paymentPreferences, StatusDetails statusDetails, List<Constraint> constraints, Date expirationTimestamp, String chargePermissionId, String chargeId, String storeId, DeliverySpecifications deliverySpecifications) {
        super(creationTimestamp, releaseEnvironment);
        this.checkoutSessionId = checkoutSessionId;
        this.chargePermissionType = chargePermissionType;
        this.recurringMetaData = recurringMetaData;
        this.webCheckoutDetails = webCheckoutDetails;
        this.productType = productType;
        this.paymentDetails = paymentDetails;
        this.merchantMetadata = merchantMetaData;
        this.platformId = platformId;
        this.providerMetadata = providerMetadata;
        this.buyer = buyer;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.paymentPreferences = paymentPreferences;
        this.statusDetails = statusDetails;
        this.constraints = constraints;
        this.expirationTimestamp = expirationTimestamp;
        this.chargePermissionId = chargePermissionId;
        this.chargeId = chargeId;
        this.storeId = storeId;
        this.deliverySpecifications = deliverySpecifications;
    }
}
