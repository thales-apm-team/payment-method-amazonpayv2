package com.payline.payment.amazonv2.utils.form;

import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.Script;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.*;
import com.payline.payment.amazonv2.utils.JsonService;
import com.payline.payment.amazonv2.utils.amazon.SignatureUtils;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;

import java.util.Locale;

public class FormUtils {
    private final JsonService jsonService = JsonService.getInstance();
    private SignatureUtils signature = SignatureUtils.getInstance();


    /**
     * Holder
     */
    private static class SingletonHolder {
        /**
         * Unique instance, not preinitializes
         */
        private static final FormUtils instance = new FormUtils();
    }

    /**
     * Unique access point for the singleton instance
     */
    public static FormUtils getInstance() {
        return SingletonHolder.instance;
    }


    /**
     * Create the Amazon pay script
     *
     * @param request Payline request used to fill data in the script
     * @return
     */
    public Script createScript(PaymentFormConfigurationRequest request) {
        RequestConfiguration configuration = RequestConfiguration.build(request);

        CheckoutSession session = CheckoutSession.builder()
                .webCheckoutDetails(WebCheckoutDetails.builder()
                        .checkoutReviewReturnUrl(request.getEnvironment().getRedirectionReturnURL())
                        .build())
                .storeId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.STORE_ID).getValue())
                .build();

        // generate signature
        signature.init(configuration);

        CreateCheckoutSessionConfig sessionConfig = CreateCheckoutSessionConfig.builder()
                .payloadJSON(jsonService.toJson(session))
                .publicKeyId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.PUBLIC_KEY_ID).getValue())
                .signature(signature.generateSignature(session))
                .build();

        return Script.builder()
                .merchantId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_ID).getValue())
                .ledgerCurrency(request.getAmount().getCurrency().getCurrencyCode())
                .sandbox(request.getEnvironment().isSandbox())
                .checkoutLanguage(getLanguage(request.getLocale()))
                .productType(ProductType.valueOf(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.PRODUCT_TYPE).getValue()))
                .placement(Placement.valueOf(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.PLACEMENT)))
                .buttonColor(ButtonColor.valueOf(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.BUTTON_COLOR).getValue()))
                .createCheckoutSessionConfig(sessionConfig)
                .build();
    }


    /**
     * get the language of a Locale Object. Only possible values are: de_DE, fr_FR, it_IT, es_ES, and en_GB
     * if the locale is not in these values, en_GB is chosen.
     *
     * @param locale variable to extract the right language
     * @return an accepted language String
     */
    String getLanguage(Locale locale) {
        String language;
        switch (locale.toString()) {
            case "de":
            case "de_DE":
                language = "de_DE";
                break;
            case "fr":
            case "fr_FR":
                language = "fr_FR";
                break;
            case "it":
            case "it_IT":
                language = "it_IT";
                break;
            case "es":
            case "es_ES":
                language = "es_ES";
                break;
            default:
                language = "en_GB";
                break;
        }

        return language;
    }

}
