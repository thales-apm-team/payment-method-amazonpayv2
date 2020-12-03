package com.payline.payment.amazonv2.utils.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestContextKeys {
    public final String CHECKOUT_SESSION_ID = "checkoutSessionId";
    public final String STEP = "step";
    public final String EMAIL = "email";

    public final String STEP_COMPLETE = "stepComplete";
}
