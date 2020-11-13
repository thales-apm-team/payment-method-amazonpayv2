package com.payline.payment.amazonv2.utils.form;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.Script;
import com.payline.payment.amazonv2.bean.nested.ButtonColor;
import com.payline.payment.amazonv2.bean.nested.Placement;
import com.payline.payment.amazonv2.bean.nested.ProductType;
import com.payline.payment.amazonv2.utils.amazon.SignatureUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;

class FormUtilsTest {
    @InjectMocks
    FormUtils formUtils = FormUtils.getInstance();

    @Mock
    SignatureUtils signatureUtils = SignatureUtils.getInstance();

    private final String buttonColor = "Gold";
    private final String checkoutLanguage = "fr_FR";
    private final String placement = "Cart";
    private final String productType = "PayOnly";
    private final String ledgerCurrency = "EUR";
    private final String merchantId = "123123";
    private final String payload = "{\"webCheckoutDetails\":{\"checkoutReviewReturnUrl\":\"https://www.redirection.url.com\"},\"storeId\":\"storeId\"}";
    private final String publicKeyId = "publicKeyId";
    private final String signature = "this is a signature";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createScript() {
        Mockito.doReturn(signature).when(signatureUtils).generateSignature(any());

        Script script = formUtils.createScript(MockUtils.aPaymentFormConfigurationRequest());

        // assertions on script values
        Assertions.assertEquals(ButtonColor.valueOf(buttonColor), script.getButtonColor());
        Assertions.assertEquals(checkoutLanguage, script.getCheckoutLanguage());
        Assertions.assertEquals(ledgerCurrency, script.getLedgerCurrency());
        Assertions.assertEquals(merchantId, script.getMerchantId());
        Assertions.assertEquals(Placement.Cart, script.getPlacement());
        Assertions.assertEquals(ProductType.PayOnly, script.getProductType());
        Assertions.assertTrue(script.isSandbox());
        Assertions.assertEquals(payload, script.getCreateCheckoutSessionConfig().getPayloadJSON());
        Assertions.assertEquals(publicKeyId, script.getCreateCheckoutSessionConfig().getPublicKeyId());
        Assertions.assertEquals(signature, script.getCreateCheckoutSessionConfig().getSignature());
    }

    @Test
    void getLanguage() {
        Assertions.assertEquals("fr_FR", formUtils.getLanguage(Locale.FRANCE));
        Assertions.assertEquals("fr_FR", formUtils.getLanguage(Locale.FRENCH));
        Assertions.assertEquals("it_IT", formUtils.getLanguage(Locale.ITALIAN));
        Assertions.assertEquals("it_IT", formUtils.getLanguage(Locale.ITALY));
        Assertions.assertEquals("es_ES", formUtils.getLanguage(new Locale("es", "ES")));
        Assertions.assertEquals("es_ES", formUtils.getLanguage(new Locale("es")));
        Assertions.assertEquals("de_DE", formUtils.getLanguage(Locale.GERMANY));
        Assertions.assertEquals("de_DE", formUtils.getLanguage(Locale.GERMAN));
        Assertions.assertEquals("en_GB", formUtils.getLanguage(Locale.ENGLISH));

        Assertions.assertEquals("en_GB", formUtils.getLanguage(Locale.CHINA));
    }
}