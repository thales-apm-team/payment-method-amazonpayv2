package com.payline.payment.amazonv2.utils.form;

import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.Script;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.*;
import com.payline.payment.amazonv2.service.RequestConfigurationService;
import com.payline.payment.amazonv2.utils.JsonService;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.SignatureUtils;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.constant.PartnerConfigurationKeys;
import com.payline.payment.amazonv2.utils.i18n.I18nService;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormDisplayFieldText;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormField;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormUtils {
    private final JsonService jsonService = JsonService.getInstance();
    private SignatureUtils signature = SignatureUtils.getInstance();
    private final I18nService i18n = I18nService.getInstance();


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
     * @return the script to load
     */
    public Script createScript(PaymentFormConfigurationRequest request) {
        RequestConfiguration configuration = RequestConfigurationService.getInstance().build(request);

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


    public PaymentFormConfigurationResponse createPaymentInfoDisplayForm(CheckoutSession session, RedirectionPaymentRequest request) {
        Locale locale = request.getLocale();
        Amount amount = request.getAmount();
        List<PaymentFormField> customFields = new ArrayList<>();

        // show buyer email
        String emailContent = createContent(i18n.getMessage("checkoutConfirmation.email", locale), session.getBuyer().getEmail());
        PaymentFormDisplayFieldText emailDisplay = PaymentFormDisplayFieldText.PaymentFormDisplayFieldTextBuilder
                .aPaymentFormDisplayFieldText()
                .withContent(emailContent)
                .build();
        customFields.add(emailDisplay);

        // show buyer name
        String nameContent = createContent(i18n.getMessage("checkoutConfirmation.name", locale), session.getBuyer().getName());
        PaymentFormDisplayFieldText nameDisplay = PaymentFormDisplayFieldText.PaymentFormDisplayFieldTextBuilder
                .aPaymentFormDisplayFieldText()
                .withContent(nameContent)
                .build();
        customFields.add(nameDisplay);

        // show amount
        String amountContent = createContent(i18n.getMessage("checkoutConfirmation.amount", locale), PluginUtils.createStringAmountToShow(amount));
        PaymentFormDisplayFieldText amountDisplay = PaymentFormDisplayFieldText.PaymentFormDisplayFieldTextBuilder
                .aPaymentFormDisplayFieldText()
                .withContent(amountContent)
                .build();
        customFields.add(amountDisplay);

        // show delivery address if present
        if (session.getShippingAddress() != null) {
            String shippingAddress = createStringAddress(session.getShippingAddress());
            String shippingAddressContent = createContent(i18n.getMessage("checkoutConfirmation.shippingAddress", locale), shippingAddress);
            PaymentFormDisplayFieldText shippingAddressDisplay = PaymentFormDisplayFieldText.PaymentFormDisplayFieldTextBuilder
                    .aPaymentFormDisplayFieldText()
                    .withContent(shippingAddressContent)
                    .build();
            customFields.add(shippingAddressDisplay);
        }

        // show recurring if present
        if (session.getRecurringMetaData() != null) {
            // todo afficher le recurring
        }

        // return form
        CustomForm form = CustomForm.builder()
                .withDescription(i18n.getMessage("checkoutConfirmation.description", locale))
                .withDisplayButton(true)
                .withButtonText(i18n.getMessage("checkoutConfirmation.buttonText", locale))
                .withCustomFields(customFields)
                .build();

        return PaymentFormConfigurationResponseSpecific.PaymentFormConfigurationResponseSpecificBuilder
                .aPaymentFormConfigurationResponseSpecific()
                .withPaymentForm(form)
                .build();
    }


    private String createContent(String message, String value) {
        return message + ": " + value;
    }

    /**
     * @param address an Address Object
     * @return an address in String
     */
    public String createStringAddress(Address address) {

        String sb = PluginUtils.addIfExist(address.getName()) +
                PluginUtils.addIfExist(address.getAddressLine1()) +
                PluginUtils.addIfExist(address.getAddressLine2()) +
                PluginUtils.addIfExist(address.getAddressLine3()) +
                PluginUtils.addIfExist(address.getCity()) +
                PluginUtils.addIfExist(address.getDistrict()) +
                PluginUtils.addIfExist(address.getStateOrRegion()) +
                PluginUtils.addIfExist(address.getPostalCode()) +
                PluginUtils.addIfExist(address.getCountryCode()) +
                PluginUtils.addIfExist(address.getPhoneNumber());
        return sb.trim();
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
