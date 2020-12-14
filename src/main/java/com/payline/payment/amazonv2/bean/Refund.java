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
    String refundId;
    String chargeId;
    Price refundAmount;
    String softDescriptor;
    StatusDetails statusDetails;


}
