package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class CreateCheckoutSessionConfig {
    @NonNull
    private final String payloadJSON;
    @NonNull
    private final String signature;
    @NonNull
    private final String publicKeyId;
}
