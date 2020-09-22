package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class AddressRestrictions {
    private Type type;
    private String restrictions;    // todo j'ai pas compris la doc... http://amazonpaycheckoutintegrationguide.s3.amazonaws.com/amazon-pay-api-v2/checkout-session.html#type-addressrestrictions

    public enum Type {
        Allowed, NotAllowed
    }
}
