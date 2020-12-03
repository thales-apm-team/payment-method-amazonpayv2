package com.payline.payment.amazonv2.bean;

import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;


@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Refund extends AmazonBean {
    private String refundId;
    private String chargeId;
    private Price refundAmount;
    private String softDescriptor;
    private StatusDetails statusDetails;


}
