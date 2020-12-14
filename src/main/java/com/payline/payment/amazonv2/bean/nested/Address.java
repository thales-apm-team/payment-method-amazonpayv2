package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Address {
    String name;
    String addressLine1;
    String addressLine2;
    String addressLine3;
    String city;
    String county;
    String district;
    String stateOrRegion;
    String postalCode;
    String countryCode;
    String phoneNumber;
}
