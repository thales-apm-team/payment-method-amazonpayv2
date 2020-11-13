package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.Address;
import com.payline.payment.amazonv2.bean.nested.PaymentDetails;
import com.payline.payment.amazonv2.bean.nested.Price;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.PluginUtils;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.amazon.ReasonCodeConverter;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.payment.amazonv2.utils.i18n.I18nService;
import com.payline.pmapi.bean.common.Amount;
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
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormDisplayFieldText;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormField;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentWithRedirectionServiceImpl.class);

    private ClientUtils client = ClientUtils.getInstance();
    private final I18nService i18n = I18nService.getInstance();

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
            response = e.toPaymentResponseFailureBuilder().build();
        }
        return response;
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
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
                .withPaymentFormConfigurationResponse(createForm(session, request))
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

        // return a final Payment response
        String email = request.getRequestContext().getRequestData().get(RequestContextKeys.EMAIL);
        BuyerPaymentId transactionDetails = Email.EmailBuilder
                .anEmail()
                .withEmail(email)
                .build();

        PaymentResponse response;
        if ("Completed".equalsIgnoreCase(session.getStatusDetails().getState())) {
            response = PaymentResponseSuccess.PaymentResponseSuccessBuilder
                    .aPaymentResponseSuccess()
                    .withPartnerTransactionId(session.getChargeId())
                    .withStatusCode(session.getStatusDetails().getState())
                    .withTransactionAdditionalData(checkoutSessionId)
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

    private PaymentFormConfigurationResponse createForm(CheckoutSession session, RedirectionPaymentRequest request) {
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
     * @param address
     * @return
     */
    public static String createStringAddress(Address address) {
        StringBuilder sb = new StringBuilder(address.getName());

        sb.append(PluginUtils.addIfExist(address.getAddressLine1()));
        sb.append(PluginUtils.addIfExist(address.getAddressLine2()));
        sb.append(PluginUtils.addIfExist(address.getAddressLine3()));
        sb.append(PluginUtils.addIfExist(address.getCity()));
        sb.append(PluginUtils.addIfExist(address.getDistrict()));
        sb.append(PluginUtils.addIfExist(address.getStateOrRegion()));
        sb.append(PluginUtils.addIfExist(address.getPostalCode()));
        sb.append(PluginUtils.addIfExist(address.getCountryCode()));
        sb.append(PluginUtils.addIfExist(address.getPhoneNumber()));

        return sb.toString();
    }
}
