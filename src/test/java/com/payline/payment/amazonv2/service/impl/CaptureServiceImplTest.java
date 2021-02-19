package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.Charge;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.nested.Buyer;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import com.payline.payment.amazonv2.bean.nested.WebCheckoutDetails;
import com.payline.payment.amazonv2.exception.InvalidDataException;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.capture.response.CaptureResponse;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseFailure;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseSuccess;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect.RedirectionRequest.RequestType.GET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
@Log4j2
class CaptureServiceImplTest {

    @InjectMocks
    private final CaptureServiceImpl service = new CaptureServiceImpl();

    @Mock
    private ClientUtils client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void captureRequest_Captured() {
        Mockito.doReturn(MockUtils.aCaptureResponse_Captured()).when(client).captureCharge(any(),any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_With_ChargeId())
                .build();

        CaptureResponse captureResponse = service.captureRequest(captureRequest);

        Assertions.assertEquals(CaptureResponseSuccess.class,captureResponse.getClass());
        Assertions.assertEquals("Captured",((CaptureResponseSuccess)captureResponse).getStatusCode());

    }

    @Test
    void captureRequest_CaptureInitiated() {
        Mockito.doReturn(MockUtils.aCaptureResponse_CaptureInitiated()).when(client).captureCharge(any(),any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_With_ChargeId())
                .build();

        CaptureResponse captureResponse = service.captureRequest(captureRequest);

        Assertions.assertEquals(CaptureResponseSuccess.class,captureResponse.getClass());
        Assertions.assertEquals("CaptureInitiated",((CaptureResponseSuccess)captureResponse).getStatusCode());

    }

    @Test
    void captureRequest_Declined() {
        Mockito.doReturn(MockUtils.aCaptureResponse_Declined()).when(client).captureCharge(any(),any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_With_ChargeId())
                .build();

        CaptureResponse captureResponse = service.captureRequest(captureRequest);

        Assertions.assertEquals(CaptureResponseSuccess.class,captureResponse.getClass());
        Assertions.assertEquals("Declined",((CaptureResponseSuccess)captureResponse).getStatusCode());

    }

    @Test
    void captureRequest_CaptureResponse_Without_StatusDetails() {
        Mockito.doReturn(MockUtils.aCaptureResponse_Without_StatusDetails()).when(client).captureCharge(any(),any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_With_ChargeId())
                .build();

        CaptureResponse captureResponse = service.captureRequest(captureRequest);

        Assertions.assertEquals(CaptureResponseFailure.class,captureResponse.getClass());
        Assertions.assertEquals(FailureCause.INVALID_DATA,((CaptureResponseFailure)captureResponse).getFailureCause());
        Assertions.assertEquals("Missing statusDetails in the CaptureResponse",((CaptureResponseFailure)captureResponse).getErrorCode());

    }

    @Test
    void captureRequest_Empty_State() {
        Mockito.doReturn(MockUtils.aCaptureResponse_Empty_State()).when(client).captureCharge(any(),any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_With_ChargeId())
                .build();

        CaptureResponse captureResponse = service.captureRequest(captureRequest);

        Assertions.assertEquals(CaptureResponseFailure.class,captureResponse.getClass());
        Assertions.assertEquals(FailureCause.INVALID_DATA,((CaptureResponseFailure)captureResponse).getFailureCause());
        Assertions.assertEquals("Unable to get the capture state",((CaptureResponseFailure)captureResponse).getErrorCode());

    }

    @Test
    void captureRequest_Null_RequestContext() {
        Mockito.doReturn(MockUtils.aCaptureResponse_Empty_State()).when(client).captureCharge(any(),any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(null)
                .build();

        PluginException e = Assertions.assertThrows(PluginException.class, () -> service.captureRequest(captureRequest));
        Assertions.assertEquals("Missing chargeId in the capture request", e.getErrorCode());

    }

    @Test
    void captureRequest_RequestContext_Without_ChargeId() {
        Mockito.doReturn(MockUtils.aCaptureResponse_Empty_State()).when(client).captureCharge(any(),any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_Without_ChargeId())
                .build();

        PluginException e = Assertions.assertThrows(PluginException.class, () -> service.captureRequest(captureRequest));
        Assertions.assertEquals("Missing chargeId in the capture request", e.getErrorCode());

    }

    @Test
    void captureRequest_PluginException() {
        Mockito.doThrow(new InvalidDataException("foo")).when(client).captureCharge(anyString(), any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_With_ChargeId())
                .build();

        CaptureResponse captureResponse = service.captureRequest(captureRequest);

        Assertions.assertEquals(CaptureResponseFailure.class, captureResponse.getClass());
        CaptureResponseFailure responseFailure = (CaptureResponseFailure) captureResponse;
        Assertions.assertEquals("foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void captureRequest_RuntimeException() {
        Mockito.doThrow(new NullPointerException("foo")).when(client).captureCharge(anyString(), any());

        CaptureRequest captureRequest = CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withAmount(MockUtils.aPaylineAmount())
                .withSoftDescriptor("Descriptor")
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(MockUtils.aContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withPartnerConfiguration(MockUtils.aPartnerConfiguration())
                .withPartnerTransactionId("123456")
                .withTransactionId("987654321")
                .withRequestContext(MockUtils.aRequestContext_With_ChargeId())
                .build();

        CaptureResponse captureResponse = service.captureRequest(captureRequest);

        Assertions.assertEquals(CaptureResponseFailure.class, captureResponse.getClass());
        CaptureResponseFailure responseFailure = (CaptureResponseFailure) captureResponse;
        Assertions.assertEquals("plugin error: NullPointerException: foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }

}