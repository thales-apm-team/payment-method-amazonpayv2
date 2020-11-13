package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.nested.Buyer;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormDisplayFieldText;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


class PaymentWithRedirectionServiceImplTest {
    @InjectMocks
    PaymentWithRedirectionServiceImpl service = new PaymentWithRedirectionServiceImpl();

    @Mock
    ClientUtils client;

    private final String checkoutSessionId = "123321";
    private final String chargeId = "1111111111";

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void finalizeRedirectionPaymentStep1Nominal() {
        CheckoutSession session = CheckoutSession.builder()
                .buyer(Buyer.builder().email("foo@bar.baz").name("Foo").build()).build();
        Mockito.doReturn(session).when(client).getCheckoutSession(any());

        Map<String, String[]> httpRequestParameters = new HashMap<>();
        httpRequestParameters.put("AmazonCheckoutSessionId", new String[]{checkoutSessionId});

        RedirectionPaymentRequest request = RedirectionPaymentRequest.builder()
                .withHttpRequestParametersMap(httpRequestParameters)
                .withLocale(Locale.FRANCE)
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withTransactionId("UNKNOWN_TRANSACTION")
                .withAmount(MockUtils.aPaylineAmount())
                .withOrder(MockUtils.aPaylineOrder())
                .withBuyer(MockUtils.aBuyer())
                .withBrowser(MockUtils.aBrowser())
                .build();
        PaymentResponse response = service.finalizeRedirectionPayment(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(PaymentResponseFormUpdated.class, response.getClass());
        PaymentResponseFormUpdated responseFormUpdated = (PaymentResponseFormUpdated) response;

        Assertions.assertEquals(PaymentFormConfigurationResponseSpecific.class, responseFormUpdated.getPaymentFormConfigurationResponse().getClass());
        PaymentFormConfigurationResponseSpecific responseSpecific = (PaymentFormConfigurationResponseSpecific) responseFormUpdated.getPaymentFormConfigurationResponse();

        Assertions.assertEquals(CustomForm.class, responseSpecific.getPaymentForm().getClass());
        CustomForm customForm = (CustomForm) responseSpecific.getPaymentForm();
        Assertions.assertEquals("Récapitulatif de la commande", customForm.getDescription());
        Assertions.assertEquals("Confirmer", customForm.getButtonText());

        Assertions.assertEquals(3, customForm.getCustomFields().size());
        Assertions.assertEquals("Email: foo@bar.baz", ((PaymentFormDisplayFieldText) customForm.getCustomFields().get(0)).getContent());
        Assertions.assertEquals("Nom: Foo", ((PaymentFormDisplayFieldText) customForm.getCustomFields().get(1)).getContent());
        Assertions.assertEquals("Montant: 10.00€", ((PaymentFormDisplayFieldText) customForm.getCustomFields().get(2)).getContent());
    }

    @Test
    void finalizeRedirectionPaymentStep2Nominal() {
        CheckoutSession session = CheckoutSession.builder()
                .buyer(Buyer.builder().email("foo@bar.baz").name("Foo").build())
                .chargeId(chargeId)
                .statusDetails(StatusDetails.builder().state("Completed").build())
                .build();
        Mockito.doReturn(session).when(client).completeCheckoutSession(anyString(), any());

        Map<String, String> requestData = new HashMap<>();
        requestData.put(RequestContextKeys.STEP, RequestContextKeys.STEP_COMPLETE);
        requestData.put(RequestContextKeys.CHECKOUT_SESSION_ID, checkoutSessionId);
        requestData.put(RequestContextKeys.EMAIL, "foo@bar.baz");

        RequestContext context = RequestContext.RequestContextBuilder
                .aRequestContext()
                .withRequestData(requestData)
                .build();

        RedirectionPaymentRequest request = RedirectionPaymentRequest.builder()
                .withRequestContext(context)
                .withLocale(Locale.FRANCE)
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withTransactionId("UNKNOWN_TRANSACTION")
                .withAmount(MockUtils.aPaylineAmount())
                .withOrder(MockUtils.aPaylineOrder())
                .withBuyer(MockUtils.aBuyer())
                .withBrowser(MockUtils.aBrowser())
                .build();

        PaymentResponse response = service.finalizeRedirectionPayment(request);
        Assertions.assertEquals(PaymentResponseSuccess.class, response.getClass());
        PaymentResponseSuccess responseSuccess = (PaymentResponseSuccess) response;

        Assertions.assertEquals(chargeId, responseSuccess.getPartnerTransactionId());   // todo confirmer avec la doc?
        Assertions.assertEquals("Completed", responseSuccess.getStatusCode());
        Assertions.assertEquals(Email.class, responseSuccess.getTransactionDetails().getClass());
        Email email = (Email) responseSuccess.getTransactionDetails();

        Assertions.assertEquals("foo@bar.baz", email.getEmail());

    }

    @Test
    void handleSessionExpired() {
    }
}