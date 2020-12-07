package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.Refund;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.service.RequestConfigurationService;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.amazon.ReasonCodeConverter;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.service.RefundService;
import lombok.extern.log4j.Log4j2;
@Log4j2
public class RefundServiceImpl implements RefundService {


    private ClientUtils client = ClientUtils.getInstance();

    @Override
    public RefundResponse refundRequest(RefundRequest request) {
        RequestConfiguration configuration = RequestConfigurationService.getInstance().build(request);
        RefundResponse response;
        String refundId = "UNKNOWN";

        try {
            // create Refund object
            Price refundAmount = Price.builder()
                    .amount(PluginUtils.createStringAmount(request.getAmount()))
                    .currencyCode(request.getAmount().getCurrency().getCurrencyCode())
                    .build();

            Refund refund = Refund.builder()
                    .chargeId(request.getPartnerTransactionId())
                    .refundAmount(refundAmount)
                    .softDescriptor(request.getSoftDescriptor())
                    .build();

            // call for a refund creation
            client.init(configuration);
            refund = client.createRefund(refund);
            refundId = refund.getRefundId();

            // synchronously ask for the refund status until it's a final status
            refund = client.getRefund(refundId);
            StatusDetails details = refund.getStatusDetails();

            if ("Refunded".equalsIgnoreCase(details.getState())) {
                response = RefundResponseSuccess.RefundResponseSuccessBuilder
                        .aRefundResponseSuccess()
                        .withPartnerTransactionId(refund.getRefundId())
                        .withStatusCode(details.getState())
                        .build();
            } else if ("Declined".equalsIgnoreCase(details.getState())) {
                response = RefundResponseFailure.RefundResponseFailureBuilder
                        .aRefundResponseFailure()
                        .withPartnerTransactionId(refund.getRefundId())
                        .withErrorCode(details.getReasonDescription())
                        .withFailureCause(ReasonCodeConverter.convert(details.getReasonCode()))
                        .build();
            } else {
                response = RefundResponseSuccess.RefundResponseSuccessBuilder
                        .aRefundResponseSuccess()
                        .withPartnerTransactionId(refund.getRefundId())
                        .withStatusCode("PENDING")
                        .build();
            }

        }catch (PluginException e){
            log.info("unable to execute RefundService#refundRequest", e);
            response = e.toRefundResponseFailureBuilder()
                    .withPartnerTransactionId(refundId)
                    .build();
        }catch (RuntimeException e){
            log.error("Unexpected plugin error", e);
            response = RefundResponseFailure.RefundResponseFailureBuilder
                    .aRefundResponseFailure()
                    .withPartnerTransactionId(refundId)
                    .withErrorCode(PluginUtils.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR).build();
        }

        return response;
    }

    @Override
    public boolean canMultiple() {
        return true;
    }

    @Override
    public boolean canPartial() {
        return true;
    }
}
