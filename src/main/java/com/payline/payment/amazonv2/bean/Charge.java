package com.payline.payment.amazonv2.bean;

import com.payline.payment.amazonv2.bean.nested.MerchantMetadata;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.ProviderMetadata;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
public class Charge extends AmazonBean {
    private String chargeId;
    private String chargePermissionId;
    private Price chargeAmount;
    private Price captureAmount;
    private Price refundedAmount;
    private String softDescriptor;
    private boolean captureNow;
    private boolean canHandlePendingAuthorization;
    private ProviderMetadata providerMetadata;
    private Date expirationTimestamp;
    private MerchantMetadata merchantMetadata;
    private StatusDetails statusDetails;
    private Price convertedAmount;
    private Double conversionRate;

    @Builder
    public Charge(Date creationTimestamp, ReleaseEnvironment releaseEnvironment, String chargeId, String chargePermissionId, Price chargeAmount, Price captureAmount, Price refundedAmount, String softDescriptor, boolean captureNow, boolean canHandlePendingAuthorization, ProviderMetadata providerMetadata, Date expirationTimestamp, MerchantMetadata merchantMetadata, StatusDetails statusDetails, Price convertedAmount, Double conversionRate) {
        super(creationTimestamp, releaseEnvironment);
        this.chargeId = chargeId;
        this.chargePermissionId = chargePermissionId;
        this.chargeAmount = chargeAmount;
        this.captureAmount = captureAmount;
        this.refundedAmount = refundedAmount;
        this.softDescriptor = softDescriptor;
        this.captureNow = captureNow;
        this.canHandlePendingAuthorization = canHandlePendingAuthorization;
        this.providerMetadata = providerMetadata;
        this.expirationTimestamp = expirationTimestamp;
        this.merchantMetadata = merchantMetadata;
        this.statusDetails = statusDetails;
        this.convertedAmount = convertedAmount;
        this.conversionRate = conversionRate;
    }
}
