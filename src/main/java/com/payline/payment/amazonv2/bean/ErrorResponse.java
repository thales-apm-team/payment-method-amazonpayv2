package com.payline.payment.amazonv2.bean;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private String reasonCode;
    private String message;
}
