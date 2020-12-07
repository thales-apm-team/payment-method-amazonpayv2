package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.Script;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.service.LogoPaymentFormConfigurationService;
import com.payline.payment.amazonv2.utils.JsonService;
import com.payline.payment.amazonv2.utils.constant.PartnerConfigurationKeys;
import com.payline.payment.amazonv2.utils.form.FormUtils;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.paymentform.bean.form.PartnerWidgetForm;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetOnPay;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetOnPayCallBack;
import com.payline.pmapi.bean.paymentform.bean.form.partnerwidget.PartnerWidgetScriptImport;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseFailure;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import lombok.extern.log4j.Log4j2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
@Log4j2
public class PaymentFormConfigurationServiceImpl extends LogoPaymentFormConfigurationService {
    public static final String FAILURE_TRANSACTION_ID = "NO TRANSACTION YET";


    private final JsonService jsonService = JsonService.getInstance();
    private FormUtils formUtils = FormUtils.getInstance();

    @Override
    public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest request) {
        Locale locale = request.getLocale();
        PaymentFormConfigurationResponse pfcResponse;

        try {
            String description = i18n.getMessage("widget.description", locale);

            URL url = new URL(request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.AMAZON_SCRIPT_URL) );
            PartnerWidgetScriptImport scriptImport = PartnerWidgetScriptImport.WidgetPartnerScriptImportBuilder
                    .aWidgetPartnerScriptImport()
                    .withAsync(false)
                    .withCache(false)
                    .withUrl(url)
                    .build();

            Script script = formUtils.createScript(request);

            // create an unused but mandatory PartnerWidgetOnPay
            PartnerWidgetOnPay onPay = PartnerWidgetOnPayCallBack.WidgetContainerOnPayCallBackBuilder
                    .aWidgetContainerOnPayCallBack()
                    .withName("unused")
                    .build();

            String scriptAfterImport = "amazon.Pay.renderButton('#AmazonPayButton', " + jsonService.toJson(script) + ")";
            PartnerWidgetForm form = PartnerWidgetForm.WidgetPartnerFormBuilder
                    .aWidgetPartnerForm()
                    .withDescription(description)
                    .withDisplayButton(false)
                    .withScriptImport(scriptImport)
                    .withLoadingScriptAfterImport(scriptAfterImport)
                    .withOnPay(onPay)    // useless as the script directly redirect the buyer but mandatory by the API (shall not be null)
                    .withPerformsAutomaticRedirection(true)
                    .build();

            pfcResponse = PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                    .aPaymentFormConfigurationResponseSpecific()
                    .withPaymentForm(form)
                    .build();


        } catch (MalformedURLException e) {
            String errorMessage = "Unable convert Amazon script url into an URL object";
            log.error(errorMessage, e);

            pfcResponse = PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder
                    .aPaymentFormConfigurationResponseFailure()
                    .withPartnerTransactionId(FAILURE_TRANSACTION_ID)
                    .withErrorCode(errorMessage)
                    .withFailureCause(FailureCause.INVALID_DATA)
                    .build();
        } catch (PluginException e) {
            log.info("unable to execute PaymentFormConfigurationServiceImpl#getPaymentFormConfiguration", e);
            pfcResponse = PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder
                    .aPaymentFormConfigurationResponseFailure()
                    .withPartnerTransactionId(FAILURE_TRANSACTION_ID)
                    .withErrorCode(e.getMessage())
                    .withFailureCause(e.getFailureCause())
                    .build();
        }catch (RuntimeException e) {
            pfcResponse = PaymentFormConfigurationResponseFailure.PaymentFormConfigurationResponseFailureBuilder
                    .aPaymentFormConfigurationResponseFailure()
                    .withPartnerTransactionId(FAILURE_TRANSACTION_ID)
                    .withErrorCode(e.getMessage())
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

        return pfcResponse;
    }
}
