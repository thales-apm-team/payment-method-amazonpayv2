package com.payline.payment.amazonv2.utils.amazon;

import com.payline.pmapi.bean.common.FailureCause;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ReasonCodeConverter {
    private final Map<String, FailureCause> reasonCodes = new HashMap<>();

    static {
        reasonCodes.put("BuyerCanceled", FailureCause.CANCEL);
        reasonCodes.put("AmazonCanceled", FailureCause.PAYMENT_PARTNER_ERROR);
        reasonCodes.put("Declined", FailureCause.REFUSED);
        reasonCodes.put("Expired", FailureCause.SESSION_EXPIRED);
        reasonCodes.put("ExpiredUnused", FailureCause.SESSION_EXPIRED);
        reasonCodes.put("MerchantCanceled", FailureCause.CANCEL);
        reasonCodes.put("SoftDeclined", FailureCause.REFUSED);
        reasonCodes.put("HardDeclined", FailureCause.REFUSED);
        reasonCodes.put("TransactionTimedOut", FailureCause.SESSION_EXPIRED);
        reasonCodes.put("AmazonRejected", FailureCause.REFUSED);
        reasonCodes.put("ProcessingFailure", FailureCause.PAYMENT_PARTNER_ERROR);
    }



    /**
     * Convert the error code into a Payline FailureCause
     *
     * @param errorCode the Amazon error code
     * @return the corresponding FailureCause or PARTNER_UNKNOWN_ERROR
     */
    public FailureCause convert(String errorCode) {
        FailureCause cause = reasonCodes.get(errorCode);
        if (cause == null) {
            cause = FailureCause.PARTNER_UNKNOWN_ERROR;
        }

        return cause;
    }

}
