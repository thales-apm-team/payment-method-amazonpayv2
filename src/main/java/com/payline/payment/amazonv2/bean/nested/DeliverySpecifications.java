package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

import java.util.List;

@Builder
public class DeliverySpecifications {
    private List<String> specialRestrictions;
    private AddressRestrictions addressRestrictions;
}
