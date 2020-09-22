package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class StatusDetails {
    private String state;
    private String reasonCode;
    private String reasonDescription;
    private String lastUpdatedTimestamp;
}
