package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.nested.Buyer;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import com.payline.payment.amazonv2.bean.nested.WebCheckoutDetails;
import com.payline.payment.amazonv2.exception.InvalidDataException;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect.RedirectionRequest.RequestType.GET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class PaymentServiceImplTest {

    @InjectMocks
    private final PaymentServiceImpl service = new PaymentServiceImpl();

    @Mock
    private ClientUtils client;

    private final String checkoutSessionId = "123456";
    private final String amazonPayUrl = "http://foo.bar/baz";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void paymentRequestNominal() {
        CheckoutSession session = CheckoutSession.builder()
                .checkoutSessionId(checkoutSessionId)
                .webCheckoutDetails(WebCheckoutDetails.builder().amazonPayRedirectUrl(amazonPayUrl).build())
                .buyer(Buyer.builder().name("Foo BAR").email("foo@bar.baz").build())
                .statusDetails(StatusDetails.builder().state("FOO").build()).build();

        Mockito.doReturn(session).when(client).updateCheckoutSession(anyString(), any());
        Map<String, String> requestData = new HashMap<>();
        requestData.put(RequestContextKeys.CHECKOUT_SESSION_ID, checkoutSessionId);
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext().withRequestData(requestData).build())
                .build();
        PaymentResponse response = service.paymentRequest(request);
        Assertions.assertEquals(PaymentResponseRedirect.class, response.getClass());

        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) response;
        Assertions.assertNotNull(responseRedirect.getRequestContext());
        Assertions.assertNotNull(responseRedirect.getRequestContext().getRequestData());
        Assertions.assertNotNull(responseRedirect.getRequestContext().getRequestData().get(RequestContextKeys.STEP));
        Assertions.assertEquals(RequestContextKeys.STEP_COMPLETE, responseRedirect.getRequestContext().getRequestData().get(RequestContextKeys.STEP));
        Assertions.assertNotNull(responseRedirect.getRedirectionRequest());
        Assertions.assertEquals(checkoutSessionId, responseRedirect.getPartnerTransactionId());
        Assertions.assertEquals(GET, responseRedirect.getRedirectionRequest().getRequestType());
        Assertions.assertEquals(amazonPayUrl, responseRedirect.getRedirectionRequest().getUrl().toString());
    }

    @Test
    void paymentRequestPluginException() {
        Mockito.doThrow(new InvalidDataException("foo")).when(client).updateCheckoutSession(anyString(), any());
        Map<String, String> requestData = new HashMap<>();
        requestData.put(RequestContextKeys.CHECKOUT_SESSION_ID, checkoutSessionId);
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext().withRequestData(requestData).build())
                .build();
        PaymentResponse response = service.paymentRequest(request);

        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals("foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestRuntimeException() {
        Mockito.doThrow(new NullPointerException("foo")).when(client).updateCheckoutSession(anyString(), any());
        Map<String, String> requestData = new HashMap<>();
        requestData.put(RequestContextKeys.CHECKOUT_SESSION_ID, checkoutSessionId);
        PaymentRequest request = MockUtils.aPaylinePaymentRequestBuilder()
                .withRequestContext(RequestContext.RequestContextBuilder.aRequestContext().withRequestData(requestData).build())
                .build();
        PaymentResponse response = service.paymentRequest(request);

        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals("plugin error: NullPointerException: foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }
}