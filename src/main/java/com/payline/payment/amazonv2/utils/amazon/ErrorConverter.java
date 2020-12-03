package com.payline.payment.amazonv2.utils.amazon;

import com.payline.pmapi.bean.common.FailureCause;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ErrorConverter {
    private final Map<String, FailureCause> errors = new HashMap<>();

    static {
        // generic errors
        errors.put("InvalidHeaderValue", FailureCause.INVALID_DATA);
        errors.put("InvalidRequest", FailureCause.INVALID_DATA);
        errors.put("InvalidParameterValue", FailureCause.INVALID_DATA);
        errors.put("InvalidRequestFormat", FailureCause.INVALID_DATA);
        errors.put("MissingHeaderValue", FailureCause.INVALID_DATA);
        errors.put("MissingParameterValue", FailureCause.INVALID_DATA);
        errors.put("UnrecognizedField", FailureCause.INVALID_DATA);
        errors.put("InvalidSandboxSimulationSpecified", FailureCause.REFUSED);
        errors.put("DuplicateIdempotencyKey", FailureCause.INVALID_DATA);
        errors.put("InvalidParameterCombination", FailureCause.INVALID_DATA);
        errors.put("CurrencyMismatch", FailureCause.INVALID_DATA);
        errors.put("InvalidAPIVersion", FailureCause.INVALID_DATA);
        errors.put("UnauthorizedAccess", FailureCause.REFUSED);
        errors.put("InvalidAuthentication", FailureCause.INVALID_DATA);
        errors.put("InvalidAccountStatus", FailureCause.REFUSED);
        errors.put("InvalidRequestSignature", FailureCause.INVALID_DATA);
        errors.put("InvalidAuthorizationToken", FailureCause.INVALID_DATA);
        errors.put("ResourceNotFound ", FailureCause.INVALID_DATA);
        errors.put("UnsupportedOperation ", FailureCause.REFUSED);
        errors.put("RequestNotSupported ", FailureCause.INVALID_DATA);
        errors.put("RequestTimeout ", FailureCause.SESSION_EXPIRED);
        errors.put("TLSVersionNotSupported ", FailureCause.INVALID_DATA);
        errors.put("TooManyRequests ", FailureCause.REFUSED);
        errors.put("InternalServerError ", FailureCause.PAYMENT_PARTNER_ERROR);
        errors.put("ServiceUnavailable ", FailureCause.PAYMENT_PARTNER_ERROR);

        // Get Checkout errors
        errors.put("ResourceNotFound", FailureCause.INVALID_DATA);

        // Update Checkout errors
        errors.put("InvalidCheckoutSessionStatus", FailureCause.REFUSED);

        // Complete Checkout errors
        errors.put("AmountMismatch", FailureCause.INVALID_DATA);
        errors.put("CheckoutSessionCanceled", FailureCause.CANCEL);
        errors.put("InvalidChargeStatus", FailureCause.INVALID_DATA);
        errors.put("HardDeclined", FailureCause.REFUSED);
        errors.put("PaymentMethodNotAllowed", FailureCause.REFUSED);
        errors.put("AmazonRejected", FailureCause.REFUSED);
        errors.put("MFANotCompleted", FailureCause.REFUSED);
        errors.put("TransactionTimedOut", FailureCause.SESSION_EXPIRED);
        errors.put("ProcessingFailure", FailureCause.PAYMENT_PARTNER_ERROR);

        // Create Refund errors
        errors.put("TransactionAmountExceeded", FailureCause.INVALID_DATA);

        errors.put("TransactionCountExceeded", FailureCause.REFUSED);
    }

    /**
     * Convert the error code into a Payline FailureCause
     *
     * @param errorCode the Amazon error code
     * @return the corresponding FailureCause or PARTNER_UNKNOWN_ERROR
     */
    public FailureCause convert(String errorCode) {
        FailureCause cause = errors.get(errorCode);

        if (cause == null) {
            cause = FailureCause.PARTNER_UNKNOWN_ERROR;
        }

        return cause;
    }


}
