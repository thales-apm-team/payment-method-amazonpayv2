package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.Refund;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import com.payline.payment.amazonv2.exception.InvalidDataException;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

class RefundServiceImplTest {

    @InjectMocks
    RefundServiceImpl service;

    @Mock
    ClientUtils client;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void refundRequestNominal() {
        RefundRequest request = MockUtils.aPaylineRefundRequestBuilder().build();

        Refund refund = Refund.builder()
                .refundId("S0234234234234-R01")
                .chargeId("S0234234234234")
                .statusDetails(StatusDetails.builder().state("Pending").build())
                .build();
        Mockito.doReturn(refund).when(client).createRefund(any());
        Mockito.doReturn(refund).when(client).getRefund(any());

        RefundResponse response = service.refundRequest(request);
        Assertions.assertEquals(RefundResponseSuccess.class, response.getClass());
        RefundResponseSuccess responseSuccess = (RefundResponseSuccess) response;
        Assertions.assertEquals("S0234234234234-R01", responseSuccess.getPartnerTransactionId());
        Assertions.assertEquals("PENDING", responseSuccess.getStatusCode());
    }

    @Test
    void refundRequestPaid() {
        RefundRequest request = MockUtils.aPaylineRefundRequestBuilder().build();

        Refund refund = Refund.builder()
                .refundId("S0234234234234-R01")
                .chargeId("S0234234234234")
                .statusDetails(StatusDetails.builder().state("Refunded").build())
                .build();
        Mockito.doReturn(refund).when(client).createRefund(any());
        Mockito.doReturn(refund).when(client).getRefund(any());

        RefundResponse response = service.refundRequest(request);
        Assertions.assertEquals(RefundResponseSuccess.class, response.getClass());
        RefundResponseSuccess responseSuccess = (RefundResponseSuccess) response;
        Assertions.assertEquals("S0234234234234-R01", responseSuccess.getPartnerTransactionId());
        Assertions.assertEquals("Refunded", responseSuccess.getStatusCode());
    }

    @Test
    void refundRequestfail() {
        RefundRequest request = MockUtils.aPaylineRefundRequestBuilder().build();

        Refund refund = Refund.builder()
                .refundId("S0234234234234-R01")
                .chargeId("S0234234234234")
                .statusDetails(StatusDetails.builder().state("Declined").reasonCode("Declined").reasonDescription("sorry").build())
                .build();
        Mockito.doReturn(refund).when(client).createRefund(any());
        Mockito.doReturn(refund).when(client).getRefund(any());

        RefundResponse response = service.refundRequest(request);
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());
        RefundResponseFailure responseFailure = (RefundResponseFailure) response;
        Assertions.assertEquals("S0234234234234-R01", responseFailure.getPartnerTransactionId());
        Assertions.assertEquals("sorry", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.REFUSED, responseFailure.getFailureCause());
    }

    @Test
    void refundRequestPluginException(){
        RefundRequest request = MockUtils.aPaylineRefundRequestBuilder().build();
        Mockito.doThrow(new InvalidDataException("foo")).when(client).createRefund(any());

        RefundResponse response = service.refundRequest(request);
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());
        RefundResponseFailure responseFailure = (RefundResponseFailure) response;
        Assertions.assertEquals("UNKNOWN", responseFailure.getPartnerTransactionId());
        Assertions.assertEquals("foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void refundRequestRuntimeException(){
        RefundRequest request = MockUtils.aPaylineRefundRequestBuilder().build();
        Mockito.doThrow(new NullPointerException("foo")).when(client).createRefund(any());

        RefundResponse response = service.refundRequest(request);
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());
        RefundResponseFailure responseFailure = (RefundResponseFailure) response;
        Assertions.assertEquals("UNKNOWN", responseFailure.getPartnerTransactionId());
        Assertions.assertEquals("plugin error: NullPointerException: foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }

    @Test
    void canMultiple() {
        Assertions.assertTrue(service.canMultiple());
    }

    @Test
    void canPartial() {
        Assertions.assertTrue(service.canPartial());
    }
}