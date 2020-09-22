package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class MerchantMetadata {
    private String merchantReferenceId;
    private String merchantStoreName;
    private String noteToBuyer;
    private String customInformation;
}
