package com.payline.payment.amazonv2.utils.amazon;

import com.amazon.pay.api.AmazonPayResponse;
import com.amazon.pay.api.WebstoreClient;
import com.amazon.pay.api.exceptions.AmazonPayClientException;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.ErrorResponse;
import com.payline.payment.amazonv2.bean.Refund;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.PaymentDetails;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.pmapi.bean.common.FailureCause;
import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONObject;

@Log4j2
public class ClientUtils extends ConfigurationUtils {

    private WebstoreClient client;

    private static class Holder {
        private static final ClientUtils instance = new ClientUtils();
    }

    public static ClientUtils getInstance() {
        return ClientUtils.Holder.instance;
    }

    @Override
    public void init(RequestConfiguration configuration) {
        try {
            super.init(configuration);
            client = new WebstoreClient(this.payConfiguration);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to init Amazon client utils";
            log.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }
    }

    /**
     * Try a getCheckoutSession with a bad checkoutSessionId to get a 404 ResourceNotFound.
     * If we get InvalidHeaderValue, it means that publicKeyId is invalid
     *
     * @return
     */
    public boolean isPublicKeyIdOk() {
        try {
            AmazonPayResponse response = client.getCheckoutSession("0");
            ErrorResponse errorResponse = jsonService.fromJson(response.getRawResponse(), ErrorResponse.class);

            if ("InvalidHeaderValue".equalsIgnoreCase(errorResponse.getReasonCode())) {
                return false;
            } else if ("ResourceNotFound".equalsIgnoreCase(errorResponse.getReasonCode())) {
                return true;
            }

        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to call for a getCheckoutSession in isPublicKeyIdOk";
            log.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }

        // this method should never get here
        String errorMessage = "this method should catch an AmazonPayClientException";
        log.error(errorMessage);
        throw new PluginException(errorMessage, FailureCause.PARTNER_UNKNOWN_ERROR);
    }


    public CheckoutSession getCheckoutSession(String checkoutSessionId) {
        try {
            // use APV2 library to call for get checkoutSession
            AmazonPayResponse response = client.getCheckoutSession(checkoutSessionId);

            // check response
            checkResponse(response);

            // return CheckoutSession object
            return jsonService.fromJson(response.getRawResponse(), CheckoutSession.class);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to call for a getCheckoutSession";
            log.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }
    }


    public CheckoutSession updateCheckoutSession(String checkoutSessionId, CheckoutSession session) {
        try {
            // convert CheckoutSession into JSONObject
            JSONObject jsonObject = jsonService.toJSONObject(jsonService.toJson(session));

            // use APV2 library to call for update checkoutSession
            AmazonPayResponse response = client.updateCheckoutSession(checkoutSessionId, jsonObject);

            // check response
            checkResponse(response);

            // return checkoutSession object
            return jsonService.fromJson(response.getRawResponse(), CheckoutSession.class);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to call for an updateCheckoutSession";
            log.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }
    }

    public CheckoutSession completeCheckoutSession(String checkoutSessionId, PaymentDetails details) {
        try {
            // convert PaymentDetails into JSONObject
            JSONObject jsonObject = jsonService.toJSONObject(jsonService.toJson(details));

            // use APV2 library to call for complete checkoutSession
            AmazonPayResponse response = client.completeCheckoutSession(checkoutSessionId, jsonObject);

            // check response
            checkResponse(response);

            // return checkoutSession object
            return jsonService.fromJson(response.getRawResponse(), CheckoutSession.class);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to call for a completeCheckoutSession";
            log.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }
    }

    public Refund createRefund(Refund refund) {
        try {
            // convert Refund into JSONObject
            JSONObject jsonObject = jsonService.toJSONObject(jsonService.toJson(refund));

            // use APV2 library to call for create refund
            AmazonPayResponse response = client.createRefund(jsonObject);

            // check response
            checkResponse(response);

            // return Refund object
            return jsonService.fromJson(response.getRawResponse(), Refund.class);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to call for a createRefund";
            log.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }
    }

    public Refund getRefund(String refundId) {
        try {
            // use APV2 library to call for get refund
            AmazonPayResponse response = client.getRefund(refundId);

            // check response
            checkResponse(response);

            // return Refund object
            return jsonService.fromJson(response.getRawResponse(), Refund.class);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to call for a getRefund";
            log.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }

    }

    /**
     * Verify if the amazonPayResponse is success. If not, create then throw an InvalidDataException
     *
     * @param response the AmazonPayResponse to verify
     */
    void checkResponse(AmazonPayResponse response) {
        if (!response.isSuccess()) {
            ErrorResponse errorResponse = jsonService.fromJson(response.getRawResponse(), ErrorResponse.class);
            String errorCode = errorResponse.getMessage();
            FailureCause cause = ErrorConverter.convert(errorCode);
            throw new PluginException(errorCode, cause);
        }
    }
}
