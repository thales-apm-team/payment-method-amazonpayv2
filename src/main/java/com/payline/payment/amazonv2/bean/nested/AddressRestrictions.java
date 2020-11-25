package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class AddressRestrictions {
    private Type type;
    private String restrictions;

    public enum Type {
        Allowed, NotAllowed
    }
}
