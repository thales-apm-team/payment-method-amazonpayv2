package com.payline.payment.amazonv2.bean.nested;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChargePermissionType {
    ONETIME("OneTime"),
    RECURRING("Recurring");
    private final String type;
}
