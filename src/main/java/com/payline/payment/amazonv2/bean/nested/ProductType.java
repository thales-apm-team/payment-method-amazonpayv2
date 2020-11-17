package com.payline.payment.amazonv2.bean.nested;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductType {
    PAYANDSHIP("PayAndShip"),
    PAYONLY("PayOnly"),
    SIGNIN("Signin");

    private final String type;

}
