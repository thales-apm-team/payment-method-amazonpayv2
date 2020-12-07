package com.payline.payment.amazonv2.bean;

import lombok.Value;

@Value
public class ErrorResponse {
    private String reasonCode;
    private String message;
}
