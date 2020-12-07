package com.payline.payment.amazonv2.exception;

import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseFailure;

/**
 * Generic exception which can be converted into the various ResponseFailure objects from the PM-API.
 */
public class PluginException extends RuntimeException {

    public static final int ERROR_CODE_MAX_LENGTH = 50;

    private final String errorCode;
    private final FailureCause failureCause;

    public PluginException(String message) {
        this(message, FailureCause.INTERNAL_ERROR);
    }

    public PluginException(String message, FailureCause failureCause) {
        super(message);
        if (PluginUtils.isEmpty(message)  || failureCause == null) {
            throw new IllegalStateException("PluginException must have a non-empty message and a failureCause");
        }
        this.errorCode = PluginUtils.truncate(message, ERROR_CODE_MAX_LENGTH);
        this.failureCause = failureCause;
    }

    public PluginException(String message, Exception cause) {
        this(message, FailureCause.INTERNAL_ERROR, cause);
    }

    public PluginException(String message, FailureCause failureCause, Exception cause) {
        super(message, cause);
        if (PluginUtils.isEmpty(message) || failureCause == null) {
            throw new IllegalStateException("PluginException must have a non-empty message and a failureCause");
        }
        this.errorCode = PluginUtils.truncate(message, ERROR_CODE_MAX_LENGTH);
        this.failureCause = failureCause;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public FailureCause getFailureCause() {
        return failureCause;
    }

    /**
     * Instantiate a builder for {@link PaymentResponseFailure}.
     * Returning a builder instead of the class instance allow subsequent complement,
     * with other fields than 'failureCause' or 'errorCode', such as 'partnerTransactionId' for example.
     *
     * @return A pre-configured builder
     */
    public PaymentResponseFailure.PaymentResponseFailureBuilder toPaymentResponseFailureBuilder() {
        return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode);
    }

    /**
     * Instantiate a builder for {@link PaymentFormConfigurationResponseFailure}.
     * Returning a builder instead of the class instance allow subsequent complement,
     * with other fields than 'failureCause' or 'errorCode', such as 'partnerTransactionId' for example.
     *
     * @return A pre-configured builder
     */
    public PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder toPaymentFormConfigurationResponseFailureBuilder() {
        return PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder.aPaymentFormConfigurationResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode);
    }

    /**
     * Instantiate a builder for {@link RefundResponseFailure}.
     * Returning a builder instead of the class instance allow subsequent complement,
     * with other fields than 'failureCause' or 'errorCode', such as 'partnerTransactionId' for example.
     *
     * @return A pre-configured builder
     */
    public RefundResponseFailure.RefundResponseFailureBuilder toRefundResponseFailureBuilder() {
        return RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode);
    }

    public ResetResponseFailure.ResetResponseFailureBuilder toResetResponseFailureBuilder() {
        return ResetResponseFailure.ResetResponseFailureBuilder.aResetResponseFailure()
                .withFailureCause(failureCause)
                .withErrorCode(errorCode);
    }



}
