package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.Charge;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.PaymentDetails;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.exception.PluginException;
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
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentWithRedirectionServiceImpl.class);

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
                LOGGER.error(errorMessage);
                response = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                        .withErrorCode(errorMessage)
                        .withFailureCause(FailureCause.INVALID_DATA)
                        .build();
            }
        } catch (PluginException e) {
            LOGGER.info("unable to execute PaymentWithRedirectionServiceImpl#finalizeRedirectionPayment", e);
            response = e.toPaymentResponseFailureBuilder().build();
        } catch (RuntimeException e){
            LOGGER.error("Unexpected plugin error", e);
            response = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR).build();
        }
        return response;
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        // voir dans quel process on est (paiement / remboursement)
        // dans le cas paiement partnerTranasctionId = ChechoutSessionID
        // dans le cas refund partnerTransactionId = RefundId

        // dans le ca paiement => getChechoutSesion
        //client.getCheckoutSession()

        // dans le cas refund => getRefund
        //client.getRefund()
        return null;
    }

    private PaymentResponse step1(RedirectionPaymentRequest request) {
        RequestConfiguration configuration = RequestConfiguration.build(request);

        // get the checkoutSessionId
        String REQUEST_PARAMETER_CSI = "AmazonCheckoutSessionId";
        String checkoutSessionId = request.getHttpRequestParametersMap().get(REQUEST_PARAMETER_CSI)[0];

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
        RequestConfiguration configuration = RequestConfiguration.build(request);

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


        PaymentResponse response;
        if ("Completed".equalsIgnoreCase(session.getStatusDetails().getState())) {
            // the payment is authorized
            response = chargeRequest(request, session, chargeAmount);

        } else {
            // return a failure Payment response
            String email = request.getRequestContext().getRequestData().get(RequestContextKeys.EMAIL);
            BuyerPaymentId transactionDetails = Email.EmailBuilder
                    .anEmail()
                    .withEmail(email)
                    .build();
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

    public PaymentResponse chargeRequest(RedirectionPaymentRequest request, CheckoutSession session, Price chargeAmount) {
        PaymentResponse response;

        Charge charge = Charge.builder()
                .chargePermissionId(session.getChargePermissionId())
                .chargeAmount(chargeAmount)
                .captureNow(true)
                .softDescriptor(request.getSoftDescriptor())
                .canHandlePendingAuthorization(false)
                .merchantMetadata(session.getMerchantMetadata())
                .build();

        charge = client.createCharge(charge);

        // return a final Payment response
        String email = request.getRequestContext().getRequestData().get(RequestContextKeys.EMAIL);
        BuyerPaymentId transactionDetails = Email.EmailBuilder
                .anEmail()
                .withEmail(email)
                .build();
        if ("Captured".equals(charge.getStatusDetails().getState())) {
            response = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                    .aPaymentResponseSuccess()
                    .withPartnerTransactionId(session.getChargeId())
                    .withStatusCode(session.getStatusDetails().getState())
                    .withTransactionDetails(transactionDetails)
                    .build();
        } else {
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
}
