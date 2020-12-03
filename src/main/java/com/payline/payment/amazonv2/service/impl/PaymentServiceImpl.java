package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.MerchantMetadata;
import com.payline.payment.amazonv2.bean.nested.PaymentDetails;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.bean.nested.WebCheckoutDetails;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.service.RequestConfigurationService;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.service.PaymentService;
import lombok.extern.log4j.Log4j2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
@Log4j2
public class PaymentServiceImpl implements PaymentService {

    private ClientUtils client = ClientUtils.getInstance();

    @Override
    public PaymentResponse paymentRequest(PaymentRequest request) {
        RequestConfiguration configuration = RequestConfigurationService.getInstance().build(request);
        PaymentResponse paymentResponse;

        // get checkout session id
        String checkoutSessionId = request.getRequestContext().getRequestData().get(RequestContextKeys.CHECKOUT_SESSION_ID);

        try {
            // update checkout session
            CheckoutSession checkoutSession = createCheckoutSessionFromPaymentRequest(configuration, request);

            client.init(configuration);
            checkoutSession = client.updateCheckoutSession(checkoutSessionId, checkoutSession);

            // check the response and return a PaymentResponse
            paymentResponse = createPaymentResponseFromCheckoutSession(checkoutSession);

        } catch (PluginException e) {
            log.info("unable to execute PaymentService#paymentRequest", e);

            paymentResponse = e.toPaymentResponseFailureBuilder().withPartnerTransactionId(checkoutSessionId).build();
        } catch (RuntimeException e){
            log.error("Unexpected plugin error", e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginUtils.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR).build();
        }
        return paymentResponse;
    }

    private CheckoutSession createCheckoutSessionFromPaymentRequest(RequestConfiguration configuration, PaymentRequest request) {
        WebCheckoutDetails webCheckoutDetails = WebCheckoutDetails.builder()
                .checkoutResultReturnUrl(configuration.getEnvironment().getRedirectionReturnURL())
                .build();

        Price chargeAmount = Price.builder()
                .amount(PluginUtils.createStringAmount(request.getAmount()))
                .currencyCode(request.getAmount().getCurrency().getCurrencyCode())
                .build();

        PaymentDetails paymentDetails = PaymentDetails.builder()
                .paymentIntent(request.isCaptureNow() ? PaymentDetails.PaymentIntent.AuthorizeWithCapture : PaymentDetails.PaymentIntent.Authorize)
                .canHandlePendingAuthorization(false)
                .softDescriptor(request.getSoftDescriptor())
                .chargeAmount(chargeAmount)
                .build();

        MerchantMetadata merchantMetadata = MerchantMetadata.builder()
                .merchantReferenceId(request.getTransactionId())
                .merchantStoreName(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_NAME).getValue())
                .noteToBuyer(request.getOrder().getReference())
                .build();

        return CheckoutSession.builder()
                .merchantMetadata(merchantMetadata)
                .webCheckoutDetails(webCheckoutDetails)
                .paymentDetails(paymentDetails)
                .build();
    }

    private PaymentResponse createPaymentResponseFromCheckoutSession(CheckoutSession checkoutSession) {
        PaymentResponse paymentResponse;
        String checkoutSessionId = checkoutSession.getCheckoutSessionId();

        try {
            Map<String, String> requestData = new HashMap<>();
            requestData.putIfAbsent(RequestContextKeys.CHECKOUT_SESSION_ID, checkoutSessionId);
            requestData.put(RequestContextKeys.EMAIL, checkoutSession.getBuyer().getEmail());
            requestData.put(RequestContextKeys.STEP, RequestContextKeys.STEP_COMPLETE);

            RequestContext requestContext = RequestContext.RequestContextBuilder
                    .aRequestContext()
                    .withRequestData(requestData)
                    .build();

            PaymentResponseRedirect.RedirectionRequest redirectionRequest = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder
                    .aRedirectionRequest()
                    .withRequestType(PaymentResponseRedirect.RedirectionRequest.RequestType.GET)
                    .withUrl(new URL(checkoutSession.getWebCheckoutDetails().getAmazonPayRedirectUrl()))
                    .build();

            paymentResponse = PaymentResponseRedirect.PaymentResponseRedirectBuilder
                    .aPaymentResponseRedirect()
                    .withPartnerTransactionId(checkoutSessionId)
                    .withRequestContext(requestContext)
                    .withRedirectionRequest(redirectionRequest)
                    .withStatusCode(checkoutSession.getStatusDetails().getState())
                    .build();
        } catch (MalformedURLException e) {
            String errorMessage = "Unable to convert AmazonPayRedirectUrl into an URL";
            log.error(errorMessage);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withPartnerTransactionId(checkoutSessionId)
                    .withErrorCode(errorMessage)
                    .withFailureCause(FailureCause.INVALID_DATA)
                    .build();
        }

        return paymentResponse;
    }
}
