package com.payline.payment.amazonv2.utils.amazon;

import com.amazon.pay.api.AmazonPayResponse;
import com.amazon.pay.api.WebstoreClient;
import com.amazon.pay.api.exceptions.AmazonPayClientException;
import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.AmazonBean;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.Refund;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.PaymentDetails;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.pmapi.bean.common.FailureCause;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class ClientUtilsTest {

    @Spy
    @InjectMocks
    ClientUtils client = ClientUtils.getInstance();

    @Mock
    WebstoreClient webstoreClient;


    String checkoutSessionId = "083a860e-4a0c-46f0-80cd-a8746ad5a0dc";
    String platformId = "this is a platform id";

    CheckoutSession expectedSession = CheckoutSession.builder()
            .checkoutSessionId(checkoutSessionId)
            .platformId(platformId)
            .releaseEnvironment(AmazonBean.ReleaseEnvironment.Sandbox).build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void initNominal() {
        RequestConfiguration configuration = new RequestConfiguration(MockUtils.aContractConfiguration()
                , MockUtils.anEnvironment()
                , MockUtils.aPartnerConfiguration());

        Mockito.doNothing().when((ConfigurationUtils) client).init(configuration);

        Assertions.assertDoesNotThrow(() -> client.init(configuration));
    }

    @Test
    void getCheckoutSessionNominal() throws Exception {
        AmazonPayResponse response = new AmazonPayResponse();
        response.setStatus(200);
        response.setRawResponse("{\n" +
                "  \"checkoutSessionId\": \"083a860e-4a0c-46f0-80cd-a8746ad5a0dc\",\n" +
                "  \"platformId\": \"this is a platform id\",\n" +
                "  \"chargePermissionId\": null,\n" +
                "  \"chargeId\": null,\n" +
                "  \"releaseEnvironment\": \"Sandbox\"\n" +
                "}");

        Mockito.doReturn(response).when(webstoreClient).getCheckoutSession(any());

        CheckoutSession receivedSession = client.getCheckoutSession(checkoutSessionId);

        Assertions.assertEquals(expectedSession.getCheckoutSessionId(), receivedSession.getCheckoutSessionId());
        Assertions.assertEquals(expectedSession.getChargeId(), receivedSession.getChargeId());
        Assertions.assertEquals(expectedSession.getChargePermissionId(), receivedSession.getChargePermissionId());
        Assertions.assertEquals(expectedSession.getReleaseEnvironment(), receivedSession.getReleaseEnvironment());
    }

    @Test
    void getCheckoutSessionException() throws Exception {
        Mockito.doThrow(new AmazonPayClientException("foo")).when(webstoreClient).getCheckoutSession(any());

        PluginException e = Assertions.assertThrows(PluginException.class, () -> client.getCheckoutSession(checkoutSessionId));
        Assertions.assertEquals("unable to call for a getCheckoutSession", e.getErrorCode());
    }

    @Test
    void updateCheckoutSessionNominal() throws Exception {
        AmazonPayResponse response = new AmazonPayResponse();
        response.setStatus(200);
        response.setRawResponse("{\n" +
                "  \"checkoutSessionId\": \"083a860e-4a0c-46f0-80cd-a8746ad5a0dc\",\n" +
                "  \"platformId\": \"this is a platform id\",\n" +
                "  \"chargePermissionId\": null,\n" +
                "  \"chargeId\": null,\n" +
                "  \"releaseEnvironment\": \"Sandbox\"\n" +
                "}");
        Mockito.doReturn(response).when(webstoreClient).updateCheckoutSession(anyString(), any());

        CheckoutSession receivedSession = client.updateCheckoutSession(checkoutSessionId, CheckoutSession.builder().build());

        Assertions.assertEquals(expectedSession.getCheckoutSessionId(), receivedSession.getCheckoutSessionId());
        Assertions.assertEquals(expectedSession.getChargeId(), receivedSession.getChargeId());
        Assertions.assertEquals(expectedSession.getChargePermissionId(), receivedSession.getChargePermissionId());
        Assertions.assertEquals(expectedSession.getReleaseEnvironment(), receivedSession.getReleaseEnvironment());

    }

    @Test
    void updateCheckoutSessionException() throws Exception {
        Mockito.doThrow(new AmazonPayClientException("foo")).when(webstoreClient).updateCheckoutSession(anyString(), any());

        PluginException e = Assertions.assertThrows(PluginException.class, () -> client.updateCheckoutSession(checkoutSessionId, CheckoutSession.builder().build()));
        Assertions.assertEquals("unable to call for an updateCheckoutSession", e.getErrorCode());

    }

    @Test
    void completeCheckoutSessionNominal() throws Exception {
        AmazonPayResponse response = new AmazonPayResponse();
        response.setStatus(200);
        response.setRawResponse("{\n" +
                "  \"checkoutSessionId\": \"083a860e-4a0c-46f0-80cd-a8746ad5a0dc\",\n" +
                "  \"platformId\": \"this is a platform id\",\n" +
                "  \"chargePermissionId\": null,\n" +
                "  \"chargeId\": null,\n" +
                "  \"releaseEnvironment\": \"Sandbox\"\n" +
                "}");
        Mockito.doReturn(response).when(webstoreClient).completeCheckoutSession(anyString(), any());

        CheckoutSession receivedSession = client.completeCheckoutSession(checkoutSessionId, PaymentDetails.builder().build());

        Assertions.assertEquals(expectedSession.getCheckoutSessionId(), receivedSession.getCheckoutSessionId());
        Assertions.assertEquals(expectedSession.getChargeId(), receivedSession.getChargeId());
        Assertions.assertEquals(expectedSession.getChargePermissionId(), receivedSession.getChargePermissionId());
        Assertions.assertEquals(expectedSession.getReleaseEnvironment(), receivedSession.getReleaseEnvironment());

    }

    @Test
    void completeCheckoutSessionException() throws Exception {
        Mockito.doThrow(new AmazonPayClientException("foo")).when(webstoreClient).completeCheckoutSession(anyString(), any());

        PluginException e = Assertions.assertThrows(PluginException.class, () -> client.completeCheckoutSession(checkoutSessionId, PaymentDetails.builder().build()));
        Assertions.assertEquals("unable to call for a completeCheckoutSession", e.getErrorCode());
    }

    @Test
    void createRefundNominal() throws Exception {
        AmazonPayResponse response = new AmazonPayResponse();
        response.setStatus(200);
        response.setRawResponse("{\n" +
                "  \"refundId\": \"083a860e-4a0c-46f0-80cd-a1z2e3r4t5y6\",\n" +
                "  \"chargeId\": \"123123321321\"\n" +
                "}");
        Mockito.doReturn(response).when(webstoreClient).createRefund(any());

        Refund receivedRefund = client.createRefund(Refund.builder().build());

        Assertions.assertEquals("083a860e-4a0c-46f0-80cd-a1z2e3r4t5y6", receivedRefund.getRefundId());
        Assertions.assertEquals("123123321321", receivedRefund.getChargeId());
    }

    @Test
    void createRefundException() throws Exception {
        Mockito.doThrow(new AmazonPayClientException("foo")).when(webstoreClient).createRefund(any());

        PluginException e = Assertions.assertThrows(PluginException.class, () -> client.createRefund(Refund.builder().build()));
        Assertions.assertEquals("unable to call for a createRefund", e.getErrorCode());
    }

    @Test
    void getRefundNominal() throws Exception {
        AmazonPayResponse response = new AmazonPayResponse();
        response.setStatus(200);
        response.setRawResponse("{\n" +
                "  \"refundId\": \"083a860e-4a0c-46f0-80cd-a1z2e3r4t5y6\",\n" +
                "  \"chargeId\": \"123123321321\"\n" +
                "}");
        Mockito.doReturn(response).when(webstoreClient).getRefund(anyString());

        Refund receivedRefund = client.getRefund("123321");

        Assertions.assertEquals("083a860e-4a0c-46f0-80cd-a1z2e3r4t5y6", receivedRefund.getRefundId());
        Assertions.assertEquals("123123321321", receivedRefund.getChargeId());
    }


    @Test
    void getRefundException() throws Exception {
        Mockito.doThrow(new AmazonPayClientException("foo")).when(webstoreClient).getRefund(any());

        PluginException e = Assertions.assertThrows(PluginException.class, () -> client.getRefund("aRefundId"));
        Assertions.assertEquals("unable to call for a getRefund", e.getErrorCode());

    }

    @Test
    void checkResponseSuccess() {
        AmazonPayResponse response = new AmazonPayResponse();
        response.setStatus(200);

        Assertions.assertDoesNotThrow(() -> client.checkResponse(response));

    }

    @Test
    void checkResponseFailure() {
        AmazonPayResponse response = new AmazonPayResponse();
        response.setStatus(400);
        response.setRawResponse("{\n" +
                "  \"reasonCode\": \"thisErrorIsUnknown\",\n" +
                "  \"message\": \"The value provided for [Parameter] is invalid.\"\n" +
                "}");

        PluginException e = Assertions.assertThrows(PluginException.class, () -> client.checkResponse(response));
        Assertions.assertEquals("The value provided for [Parameter] is invalid.", e.getErrorCode());
        Assertions.assertEquals(FailureCause.PARTNER_UNKNOWN_ERROR, e.getFailureCause());
    }
}