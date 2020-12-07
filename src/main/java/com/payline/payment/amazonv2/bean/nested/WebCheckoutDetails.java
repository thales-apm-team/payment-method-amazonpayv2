package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class WebCheckoutDetails {
    private String checkoutReviewReturnUrl;
    private String checkoutResultReturnUrl;
    private String amazonPayRedirectUrl;
}
