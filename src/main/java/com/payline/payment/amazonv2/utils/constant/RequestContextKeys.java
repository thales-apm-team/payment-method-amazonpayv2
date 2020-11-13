package com.payline.payment.amazonv2.utils.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RequestContextKeys {
    public static final String CHECKOUT_SESSION_ID = "checkoutSessionId";
    public static final String STEP = "step";
    public static final String EMAIL = "email";

    public static final String STEP_COMPLETE = "stepComplete";
}
