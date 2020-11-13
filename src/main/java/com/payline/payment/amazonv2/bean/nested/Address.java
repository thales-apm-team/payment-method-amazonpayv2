package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Address {
    private String name;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String county;
    private String district;
    private String stateOrRegion;
    private String postalCode;
    private String countryCode;
    private String phoneNumber;
}
