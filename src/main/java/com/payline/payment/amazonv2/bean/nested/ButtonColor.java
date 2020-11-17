package com.payline.payment.amazonv2.bean.nested;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ButtonColor {
    GOLD("Gold"),
    LIGHTGRAY("LightGray"),
    DARKGRAY("DarkGray");

    private final String color;

}
