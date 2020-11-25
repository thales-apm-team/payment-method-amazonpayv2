package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.Script;
import com.payline.payment.amazonv2.bean.nested.ButtonColor;
import com.payline.payment.amazonv2.bean.nested.CreateCheckoutSessionConfig;
import com.payline.payment.amazonv2.bean.nested.Placement;
import com.payline.payment.amazonv2.bean.nested.ProductType;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.form.FormUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.paymentform.bean.form.PartnerWidgetForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URL;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;

class PaymentFormConfigurationServiceImplTest {
    @InjectMocks
    PaymentFormConfigurationServiceImpl service = new PaymentFormConfigurationServiceImpl();

    @Mock
    FormUtils formUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getPaymentFormConfiguration() throws Exception {
        String payload = "{\"webCheckoutDetails\":{\"checkoutReviewReturnUrl\":\"http://redirectionURL.com\"},\"storeId\":\"storeId\"}";

        Script mockedScript = Script.builder()
                .createCheckoutSessionConfig(CreateCheckoutSessionConfig.builder()
                        .payloadJSON(payload)
                        .publicKeyId("this is a key")
                        .signature("this is a signature")
                        .build())
                .buttonColor(ButtonColor.Gold)
                .placement(Placement.Cart)
                .productType(ProductType.PayOnly)
                .checkoutLanguage("fr_FR")
                .ledgerCurrency("EUR")
                .merchantId("123123")
                .sandbox(true)
                .build();
        Mockito.doReturn(mockedScript).when(formUtils).createScript(any());

        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(MockUtils.aPaymentFormConfigurationRequest());

        Assertions.assertEquals(PaymentFormConfigurationResponseSpecific.class, response.getClass());
        PaymentFormConfigurationResponseSpecific responseSpecific = (PaymentFormConfigurationResponseSpecific) response;

        Assertions.assertEquals("Payer avec Amazon Pay", responseSpecific.getPaymentForm().getDescription());
        Assertions.assertEquals(PartnerWidgetForm.class, responseSpecific.getPaymentForm().getClass());
        PartnerWidgetForm form = (PartnerWidgetForm) responseSpecific.getPaymentForm();

        Assertions.assertNull(form.getContainer());

        // No assertions on script
        Assertions.assertEquals(new URL("https://static-eu.payments-amazon.com/checkout.js"), form.getScriptImport().getUrl());
    }


    @Test
    void getPaymentFormConfigurationBadUrl() {
        PaymentFormConfigurationRequest request = MockUtils.aPaymentFormConfigurationRequestBuilder()
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();

        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(request);

        Assertions.assertEquals(PaymentFormConfigurationResponseFailure.class, response.getClass());
        PaymentFormConfigurationResponseFailure responseFailure = (PaymentFormConfigurationResponseFailure) response;

        Assertions.assertEquals("NO TRANSACTION YET", responseFailure.getPartnerTransactionId());
        Assertions.assertEquals("Unable convert Amazon script url into an URL object", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void getPaymentFormConfigurationPluginException(){
        Mockito.doThrow(new PluginException("foo", FailureCause.INVALID_DATA)).when(formUtils).createScript(any());

        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(MockUtils.aPaymentFormConfigurationRequest());
        Assertions.assertEquals(PaymentFormConfigurationResponseFailure.class, response.getClass());
        PaymentFormConfigurationResponseFailure responseFailure = (PaymentFormConfigurationResponseFailure) response;

        Assertions.assertEquals("NO TRANSACTION YET", responseFailure.getPartnerTransactionId());
        Assertions.assertEquals("foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void getPaymentFormConfigurationRuntimeException(){
        Mockito.doThrow(new NullPointerException("foo")).when(formUtils).createScript(any());

        PaymentFormConfigurationResponse response = service.getPaymentFormConfiguration(MockUtils.aPaymentFormConfigurationRequest());
        Assertions.assertEquals(PaymentFormConfigurationResponseFailure.class, response.getClass());
        PaymentFormConfigurationResponseFailure responseFailure = (PaymentFormConfigurationResponseFailure) response;

        Assertions.assertEquals("NO TRANSACTION YET", responseFailure.getPartnerTransactionId());
        Assertions.assertEquals("foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }
}