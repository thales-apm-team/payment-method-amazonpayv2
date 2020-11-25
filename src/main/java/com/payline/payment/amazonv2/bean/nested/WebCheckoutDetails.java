package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WebCheckoutDetails {
    private String checkoutReviewReturnUrl;
    private String checkoutResultReturnUrl;
    private String amazonPayRedirectUrl;
}
