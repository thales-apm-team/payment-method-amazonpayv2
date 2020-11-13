package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Buyer {
    private String buyerId;
    private String name;
    private String email;
}
