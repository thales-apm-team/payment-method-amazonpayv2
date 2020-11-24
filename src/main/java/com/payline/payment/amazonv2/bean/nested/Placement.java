package com.payline.payment.amazonv2.bean.nested;


public enum Placement {
    Home,       // Initial or main page
    Product,  // Product details page
    Cart,    // Cart review page before buyer starts checkout
    Checkout, // Any page after buyer starts checkout
    Other;   // Any page that doesn't fit the previous descriptions
    }
