package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StatusDetails {
    private String state;
    private String reasonCode;
    private String reasonDescription;
    private String lastUpdatedTimestamp;
}
