package com.payline.payment.amazonv2.bean.nested;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Placement {
    HOME("Home"),       // Initial or main page
    PRODUCT("Product"),  // Product details page
    CART("Cart"),    // Cart review page before buyer starts checkout
    CHECKOUT("Checkout"), // Any page after buyer starts checkout
    OTHER("Other");   // Any page that doesn't fit the previous descriptions

    private final String place;
    }
