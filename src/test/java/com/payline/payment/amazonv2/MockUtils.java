package com.payline.payment.amazonv2;

import com.payline.payment.amazonv2.bean.nested.ButtonColor;
import com.payline.payment.amazonv2.bean.nested.Placement;
import com.payline.payment.amazonv2.bean.nested.ProductType;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.reset.request.ResetRequest;
import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.util.*;

@SuppressWarnings("WeakerAccess")
@UtilityClass
public class MockUtils {
    private final String TRANSACTIONID = "123456789012345678901";
    private final String PARTNER_TRANSACTIONID = "098765432109876543210";


    /**
     * Generate a valid {@link Environment}.
     */
    public Environment anEnvironment() {
        return new Environment("http://notificationURL.com",
                "https://www.redirection.url.com",
                "http://redirectionCancelURL.com",
                true);
    }

    /**
     * Generate a valid {@link PartnerConfiguration}.
     */
    public PartnerConfiguration aPartnerConfiguration() {
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(PartnerConfigurationKeys.AMAZON_SCRIPT_URL, "https://static-eu.payments-amazon.com/checkout.js");
        partnerConfigurationMap.put(PartnerConfigurationKeys.PLACEMENT, Placement.Cart.name());

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();
        sensitiveConfigurationMap.put(PartnerConfigurationKeys.PRIVATE_KEY, "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC73EuiXbAXb2Jl\n" +
                "/cf8ZUvOcNz8bqHdyPPAEXtM1VL6UB4CHDshMz6KtIrjJtXXuXcg1+Owi7qtxcym\n" +
                "DueDVXNQWmWoP3MHEVBInvKy/13xdiBmfeIzRvfmTg329tTgxo7m8MGyr2iK8Zjs\n" +
                "K6ZenqU8eOFWYiQ8UrRxsA==\n" +
                "-----END PRIVATE KEY-----");


        return new PartnerConfiguration(partnerConfigurationMap, sensitiveConfigurationMap);
    }

    /**
     * Generate a valid {@link PaymentFormLogoRequest}.
     */
    public PaymentFormLogoRequest aPaymentFormLogoRequest() {
        return PaymentFormLogoRequest.PaymentFormLogoRequestBuilder.aPaymentFormLogoRequest()
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withPartnerConfiguration(aPartnerConfiguration())
                .withLocale(Locale.getDefault())
                .build();
    }

    /**
     * Generate a valid, but not complete, {@link Order}
     */
    public Order aPaylineOrder() {
        List<Order.OrderItem> items = new ArrayList<>();

        items.add(Order.OrderItem.OrderItemBuilder
                .anOrderItem()
                .withReference("foo")
                .withAmount(aPaylineAmount())
                .withQuantity((long) 1)
                .build());

        return Order.OrderBuilder.anOrder()
                .withDate(new Date())
                .withAmount(aPaylineAmount())
                .withItems(items)
                .withReference("ORDER-REF-123456")
                .build();
    }

    /**
     * Generate a valid Payline Amount.
     */
    public com.payline.pmapi.bean.common.Amount aPaylineAmount() {
        return new com.payline.pmapi.bean.common.Amount(BigInteger.valueOf(1000), Currency.getInstance("EUR"));
    }

    /**
     * @return a valid user agent.
     */
    public String aUserAgent() {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0";
    }

    /**
     * Generate a valid {@link Browser}.
     */
    public Browser aBrowser() {
        return Browser.BrowserBuilder.aBrowser()
                .withLocale(Locale.getDefault())
                .withIp("192.168.0.1")
                .withUserAgent(aUserAgent())
                .build();
    }

    /**
     * Generate a valid {@link Buyer}.
     */
    public Buyer aBuyer() {
        return Buyer.BuyerBuilder.aBuyer()
                .withFullName(new Buyer.FullName("Marie", "Durand", "1"))
                .withEmail("foo@bar.baz")
                .build();
    }

    /**
     * Generate a valid {@link PaymentFormContext}.
     */
    public PaymentFormContext aPaymentFormContext() {
        Map<String, String> paymentFormParameter = new HashMap<>();

        return PaymentFormContext.PaymentFormContextBuilder.aPaymentFormContext()
                .withPaymentFormParameter(paymentFormParameter)
                .withSensitivePaymentFormParameter(new HashMap<>())
                .build();
    }

    /**
     * Generate a valid {@link ContractParametersCheckRequest}.
     */
    public ContractParametersCheckRequest aContractParametersCheckRequest() {
        return aContractParametersCheckRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link ContractParametersCheckRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public ContractParametersCheckRequest.CheckRequestBuilder aContractParametersCheckRequestBuilder() {
        return ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                .withAccountInfo(anAccountInfo())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withLocale(Locale.getDefault())
                .withPartnerConfiguration(aPartnerConfiguration());
    }

    /**
     * Generate a valid {@link PaymentFormConfigurationRequest}.
     */
    public PaymentFormConfigurationRequest aPaymentFormConfigurationRequest() {
        return aPaymentFormConfigurationRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link PaymentFormConfigurationRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder aPaymentFormConfigurationRequestBuilder() {
        return PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest()
                .withAmount(aPaylineAmount())
                .withBuyer(aBuyer())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withLocale(Locale.FRANCE)
                .withOrder(aPaylineOrder())
                .withPartnerConfiguration(aPartnerConfiguration());
    }

    /**
     * Generate a valid {@link PaymentRequest}.
     */
    public PaymentRequest aPaylinePaymentRequest() {
        return aPaylinePaymentRequestBuilder().build();
    }

    /**
     * Generate a builder for a valid {@link PaymentRequest}.
     * This way, some attributes may be overridden to match specific test needs.
     */
    public PaymentRequest.Builder aPaylinePaymentRequestBuilder() {
        return PaymentRequest.builder()
                .withAmount(aPaylineAmount())
                .withBrowser(aBrowser())
                .withBuyer(aBuyer())
                .withCaptureNow(true)
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withLocale(Locale.getDefault())
                .withOrder(aPaylineOrder())
                .withPartnerConfiguration(aPartnerConfiguration())
                .withPaymentFormContext(aPaymentFormContext())
                .withSoftDescriptor("softDescriptor")
                .withTransactionId(TRANSACTIONID);
    }

    public RefundRequest aPaylineRefundRequest() {
        return aPaylineRefundRequestBuilder().build();
    }

    public RefundRequest.RefundRequestBuilder aPaylineRefundRequestBuilder() {
        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(aPaylineAmount())
                .withOrder(aPaylineOrder())
                .withBuyer(aBuyer())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withTransactionId(TRANSACTIONID)
                .withPartnerTransactionId(PARTNER_TRANSACTIONID)
                .withPartnerConfiguration(aPartnerConfiguration());
    }

    public ResetRequest.ResetRequestBuilder aPaylineResetRequestBuilder() {
        return ResetRequest.ResetRequestBuilder.aResetRequest()
                .withAmount(aPaylineAmount())
                .withOrder(aPaylineOrder())
                .withBuyer(aBuyer())
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withTransactionId(TRANSACTIONID)
                .withPartnerTransactionId(PARTNER_TRANSACTIONID)
                .withPartnerConfiguration(aPartnerConfiguration());
    }

    public TransactionStatusRequest.TransactionStatusRequestBuilder aPaylineTransactionStatusRequestBuilder(){
        return TransactionStatusRequest.TransactionStatusRequestBuilder
                .aNotificationRequest()
                .withTransactionId(TRANSACTIONID)
                .withContractConfiguration(aContractConfiguration())
                .withEnvironment(anEnvironment())
                .withPartnerConfiguration(aPartnerConfiguration())
                .withAmount(aPaylineAmount())
                .withBuyer(aBuyer())
                .withOrder(aPaylineOrder());

    }

    public TransactionStatusRequest aPaylineTransactionStatusRequest(){
        return aPaylineTransactionStatusRequestBuilder().build();
    }


    /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance.
     */
    public Map<String, String> anAccountInfo() {
        return anAccountInfo(aContractConfiguration());
    }
    /**------------------------------------------------------------------------------------------------------------------*/

    /**
     * Generate a valid accountInfo, an attribute of a {@link ContractParametersCheckRequest} instance,
     * from the given {@link ContractConfiguration}.
     *
     * @param contractConfiguration The model object from which the properties will be copied
     */
    public Map<String, String> anAccountInfo(ContractConfiguration contractConfiguration) {
        Map<String, String> accountInfo = new HashMap<>();
        for (Map.Entry<String, ContractProperty> entry : contractConfiguration.getContractProperties().entrySet()) {
            accountInfo.put(entry.getKey(), entry.getValue().getValue());
        }
        return accountInfo;
    }

    /**
     * Generate a valid {@link ContractConfiguration}.
     */
    public ContractConfiguration aContractConfiguration() {
        Map<String, ContractProperty> contractProperties = new HashMap<>();

        contractProperties.put(ContractConfigurationKeys.MERCHANT_ID, new ContractProperty("123123"));
        contractProperties.put(ContractConfigurationKeys.MERCHANT_NAME, new ContractProperty("merchantName"));
        contractProperties.put(ContractConfigurationKeys.STORE_ID, new ContractProperty("storeId"));
        contractProperties.put(ContractConfigurationKeys.PUBLIC_KEY_ID, new ContractProperty("publicKeyId"));
        contractProperties.put(ContractConfigurationKeys.BUTTON_COLOR, new ContractProperty(ButtonColor.Gold.name()));
        contractProperties.put(ContractConfigurationKeys.PRODUCT_TYPE, new ContractProperty(ProductType.PayOnly.name()));

        return new ContractConfiguration("AmazonPayV2", contractProperties);
    }

}
