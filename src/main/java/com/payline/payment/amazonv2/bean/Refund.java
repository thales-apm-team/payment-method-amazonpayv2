package com.payline.payment.amazonv2.bean;

import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;


@Getter
public class Refund extends AmazonBean {
    private String refundId;
    private String chargeId;
    private Price refundAmount;
    private String softDescriptor;
    private StatusDetails statusDetails;    // todo voir si il faut un RefundStatusDetails

    @Builder
    public Refund(Date creationTimestamp, ReleaseEnvironment releaseEnvironment, String refundId, String chargeId, Price refundAmount, String softDescriptor, StatusDetails statusDetails) {
        super(creationTimestamp, releaseEnvironment);
        this.refundId = refundId;
        this.chargeId = chargeId;
        this.refundAmount = refundAmount;
        this.softDescriptor = softDescriptor;
        this.statusDetails = statusDetails;
    }
}
