package com.payline.payment.amazonv2.service.impl;


import com.payline.payment.amazonv2.bean.Charge;
import com.payline.payment.amazonv2.bean.nested.PaymentDetails;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.exception.InvalidDataException;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.capture.response.CaptureResponse;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseFailure;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseSuccess;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.service.CaptureService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CaptureServiceImpl implements CaptureService {

    private ClientUtils client = ClientUtils.getInstance();

    @Override
    public CaptureResponse captureRequest(CaptureRequest captureRequest) {
        CaptureResponse captureResponse;

        // get transactionId from capture request
        String chargeId = captureRequest.getRequestContext().getRequestData().get(RequestContextKeys.CHARGE_ID);

        if(PluginUtils.isEmpty(chargeId)){
            throw new InvalidDataException("Missing chargeId in the capture request");
        }

        try{
            PaymentDetails paymentDetails = createPaymentDetailsFromCaptureRequest(captureRequest);

            // Call captureCharge
            Charge captureChargeResponse = client.captureCharge(chargeId,paymentDetails);

            if(captureChargeResponse.getStatusDetails() == null){
                captureResponse = createFailure(captureRequest.getTransactionId(),"Missing statusDetails in the CaptureResponse",FailureCause.INVALID_DATA);
            }else {
                if (PluginUtils.isEmpty(captureChargeResponse.getStatusDetails().getState())) {
                    captureResponse = createFailure(captureRequest.getTransactionId(),"Unable to get the capture state",FailureCause.INVALID_DATA);
                } else {
                    captureResponse = CaptureResponseSuccess.CaptureResponseSuccessBuilder.aCaptureResponseSuccess()
                            .withStatusCode(captureChargeResponse.getStatusDetails().getState())
                            .withPartnerTransactionId(captureRequest.getPartnerTransactionId())
                            .build();
                }
            }

        } catch (PluginException e) {
            log.info("unable to execute CaptureService#captureRequest", e);
            captureResponse = createFailure(captureRequest.getTransactionId(),e.getErrorCode(), e.getFailureCause());
        } catch (RuntimeException e){
            log.error("Unexpected plugin error", e);
            captureResponse = createFailure(captureRequest.getTransactionId(),PluginUtils.runtimeErrorCode(e), FailureCause.INTERNAL_ERROR);
        }
        return captureResponse;
    }


    private CaptureResponseFailure createFailure(String transactionId, String errorCode, FailureCause cause) {
        return CaptureResponseFailure.CaptureResponseFailureBuilder.aCaptureResponseFailure()
                .withPartnerTransactionId(transactionId)
                .withErrorCode(errorCode)
                .withFailureCause(cause)
                .build();
    }

    private PaymentDetails createPaymentDetailsFromCaptureRequest(CaptureRequest request){

        if(request.getAmount() == null || request.getRequestContext() == null || request.getRequestContext().getRequestData() == null){
            throw new InvalidDataException("Missing required information in the capture request");
        }

        String chargeId = request.getRequestContext().getRequestData().get(RequestContextKeys.CHARGE_ID);
        if(PluginUtils.isEmpty(chargeId)){
            throw new InvalidDataException("Missing chargeId in the capture request");
        }

        Price amount = Price.builder()
                .amount(request.getAmount().getAmountInSmallestUnit().toString())
                .currencyCode(request.getAmount().getCurrency().getCurrencyCode())
                .build();

        String descriptor = "";
        if(!PluginUtils.isEmpty(request.getSoftDescriptor())){
            descriptor = request.getSoftDescriptor();
        }
        return PaymentDetails.builder()
                .chargeAmount(amount)
                .softDescriptor(descriptor)
                .build();
    }
    @Override
    public boolean canMultiple() {
        return false;
    }

    @Override
    public boolean canPartial() {
        return false;
    }

}
