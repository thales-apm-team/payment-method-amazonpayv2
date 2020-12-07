package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.Refund;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.PaymentDetails;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.StatusDetails;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.service.RequestConfigurationService;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.amazon.ReasonCodeConverter;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.payment.amazonv2.utils.form.FormUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.BuyerPaymentId;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {


    private ClientUtils client = ClientUtils.getInstance();
    FormUtils formUtils = FormUtils.getInstance();

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest request) {
        PaymentResponse response;
        try {
            // get step
            String step = request.getRequestContext().getRequestData().get(RequestContextKeys.STEP);
            if (PluginUtils.isEmpty(step)) {
                response = step1(request);
            } else if (RequestContextKeys.STEP_COMPLETE.equalsIgnoreCase(step)) {
                response = step2(request);
            } else {
                String errorMessage = "Unknown step " + step;
                log.error(errorMessage);
                response = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                        .withErrorCode(errorMessage)
                        .withFailureCause(FailureCause.INVALID_DATA)
                        .build();
            }
        } catch (PluginException e) {
            log.info("unable to execute PaymentWithRedirectionServiceImpl#finalizeRedirectionPayment", e);
            response = e.toPaymentResponseFailureBuilder().build();
        } catch (RuntimeException e) {
            log.error("Unexpected plugin error", e);
            response = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginUtils.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR).build();
        }
        return response;
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest request) {
        PaymentResponse response;

        // verify if it's a payment or a refund partnerTransactionId
        String transactionId = request.getTransactionId();

        RequestConfiguration configuration = RequestConfigurationService.getInstance().build(request);
        client.init(configuration);

        if (transactionId.startsWith("S")) {
            // refundTransactionId
            Refund refund = client.getRefund(transactionId);
            response = createPaymentResponseFromRefund(refund);
        } else {
            // payment transactionId
            CheckoutSession session = client.getCheckoutSession(transactionId);
            String email = session.getBuyer().getEmail();
            response = createPaymentResponseFromCheckoutSession(session, email);
        }

        return response;
    }

    private PaymentResponse step1(RedirectionPaymentRequest request) {
        RequestConfiguration configuration = RequestConfigurationService.getInstance().build(request);

        // get the checkoutSessionId
        String amazonCheckoutSessionId = "AmazonCheckoutSessionId";
        String checkoutSessionId = request.getHttpRequestParametersMap().get(amazonCheckoutSessionId)[0];

        // get the checkoutSession
        client.init(configuration);
        CheckoutSession session = client.getCheckoutSession(checkoutSessionId);

        // return a confirm form
        Map<String, String> requestData = new HashMap<>();
        requestData.put(RequestContextKeys.CHECKOUT_SESSION_ID, checkoutSessionId);
        requestData.put(RequestContextKeys.EMAIL, session.getBuyer().getEmail());
        RequestContext context = RequestContext.RequestContextBuilder
                .aRequestContext()
                .withRequestData(requestData)
                .build();

        return PaymentResponseFormUpdated.PaymentResponseFormUpdatedBuilder
                .aPaymentResponseFormUpdated()
                .withPaymentFormConfigurationResponse(formUtils.createPaymentInfoDisplayForm(session, request))
                .withRequestContext(context)
                .build();
    }

    private PaymentResponse step2(RedirectionPaymentRequest request) {
        RequestConfiguration configuration = RequestConfigurationService.getInstance().build(request);

        // get the checkoutSessionId
        String checkoutSessionId = request.getRequestContext().getRequestData().get(RequestContextKeys.CHECKOUT_SESSION_ID);

        // complete the checkoutSession
        Price chargeAmount = Price.builder()
                .amount(PluginUtils.createStringAmount(request.getAmount()))
                .currencyCode(request.getAmount().getCurrency().getCurrencyCode()).build();

        PaymentDetails details = PaymentDetails.builder()
                .chargeAmount(chargeAmount)
                .build();

        client.init(configuration);
        CheckoutSession session = client.completeCheckoutSession(checkoutSessionId, details);
        String email = request.getRequestContext().getRequestData().get(RequestContextKeys.EMAIL);

        return createPaymentResponseFromCheckoutSession(session, email);
    }


    private PaymentResponse createPaymentResponseFromCheckoutSession(CheckoutSession session, String email) {
        PaymentResponse response;

        BuyerPaymentId transactionDetails = Email.EmailBuilder
                .anEmail()
                .withEmail(email)
                .build();

        if ("Completed".equalsIgnoreCase(session.getStatusDetails().getState())) {
            // the payment is authorized
            response = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                    .aPaymentResponseSuccess()
                    .withPartnerTransactionId(session.getChargeId())
                    .withStatusCode(session.getStatusDetails().getState())
                    .withTransactionDetails(transactionDetails)
                    .build();
        } else {
            // failure
            response = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withPartnerTransactionId(session.getChargeId())
                    .withErrorCode(session.getStatusDetails().getReasonDescription())
                    .withFailureCause(ReasonCodeConverter.convert(session.getStatusDetails().getReasonCode()))
                    .withTransactionDetails(transactionDetails)
                    .build();
        }

        return response;
    }

    private PaymentResponse createPaymentResponseFromRefund(Refund refund) {
        PaymentResponse response;
        StatusDetails details = refund.getStatusDetails();

        if ("Refunded".equalsIgnoreCase(details.getState())) {
            response = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                    .aPaymentResponseSuccess()
                    .withPartnerTransactionId(refund.getRefundId())
                    .withStatusCode(details.getState())
                    .withTransactionDetails(new EmptyTransactionDetails())
                    .build();
        } else if ("Declined".equalsIgnoreCase(details.getState())) {
            response = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withPartnerTransactionId(refund.getRefundId())
                    .withErrorCode(details.getReasonDescription())
                    .withFailureCause(ReasonCodeConverter.convert(details.getReasonCode()))
                    .build();
        } else {
            response = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                    .aPaymentResponseSuccess()
                    .withPartnerTransactionId(refund.getRefundId())
                    .withStatusCode("PENDING")
                    .withTransactionDetails(new EmptyTransactionDetails())
                    .build();
        }
        return response;
    }
}
