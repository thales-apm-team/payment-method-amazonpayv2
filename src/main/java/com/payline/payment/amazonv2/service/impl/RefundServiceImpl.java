package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.Refund;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.amazon.ReasonCodeConverter;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.RefundService;
import org.apache.logging.log4j.Logger;

public class RefundServiceImpl implements RefundService {
    private static final Logger LOGGER = LogManager.getLogger(ConfigurationServiceImpl.class);

    private final ClientUtils client = ClientUtils.getInstance();

    @Override
    public RefundResponse refundRequest(RefundRequest request) {
        RequestConfiguration configuration = RequestConfiguration.build(request);
        RefundResponse response = null;

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
        String refundId = refund.getRefundId();

        // synchronously ask for the refund status until it's a final status
        int tryCounter = 5; // todo a definir
        while (tryCounter > 0) {
            try {
                wait(1000);// todo a definir
            } catch (InterruptedException e) {
                // todo on fait quoi?
            }

            refund = client.getRefund(refundId);
            StatusDetails details = refund.getStatusDetails();

            if ("Refunded".equalsIgnoreCase(details.getState())) {
                response = RefundResponseSuccess.RefundResponseSuccessBuilder
                        .aRefundResponseSuccess()
                        .withPartnerTransactionId(refund.getChargeId()) // todo a confirmer (cest ptet le refundId)
                        .withStatusCode(details.getState())
                        .build();
                break;
            } else if ("Declined".equalsIgnoreCase(details.getState())) {
                response = RefundResponseFailure.RefundResponseFailureBuilder
                        .aRefundResponseFailure()
                        .withPartnerTransactionId("") // todo ajouter documentation
                        .withErrorCode(details.getReasonDescription())
                        .withFailureCause(ReasonCodeConverter.convert(details.getReasonCode()))
                        .build();
                break;
            } else {
                tryCounter--;
            }
        }

        // verify if final status or end of loop
        if (response == null) {
            String errorMessage = "Unable to obtain a final refund status";
            LOGGER.error(errorMessage);
            response = RefundResponseFailure.RefundResponseFailureBuilder
                    .aRefundResponseFailure()
                    .withPartnerTransactionId(request.getPartnerTransactionId())
                    .withErrorCode(errorMessage)
                    .withFailureCause(FailureCause.SESSION_EXPIRED)
                    .build();
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
