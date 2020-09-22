package com.payline.payment.amazonv2.bean.nested;

import lombok.Builder;

@Builder
public class Buyer {
    private String buyerId;
    private String name;
    private String email;
}
