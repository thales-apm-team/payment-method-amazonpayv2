package com.payline.payment.amazonv2.service;

import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.request.RetrievePluginConfigurationRequest;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.reset.request.ResetRequest;

import java.util.Map;

public class RequestConfigurationService {

    // --- Singleton Holder pattern + initialization BEGIN
    private RequestConfigurationService() {

    }

    private static class Holder {
        private static final RequestConfigurationService INSTANCE = new RequestConfigurationService();
    }

    public static RequestConfigurationService getInstance() {
        return RequestConfigurationService.Holder.INSTANCE;
    }
    // --- Singleton Holder pattern + initialization END



    public RequestConfiguration build(CaptureRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(ContractParametersCheckRequest request) {

        ContractConfiguration configuration = request.getContractConfiguration();
        for (Map.Entry<String, String> info : request.getAccountInfo().entrySet()) {
            configuration.getContractProperties().put(info.getKey(), new ContractProperty(info.getValue()));

        }

        return new RequestConfiguration(configuration, request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(NotificationRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(PaymentFormConfigurationRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(PaymentRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(RefundRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(ResetRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(RetrievePluginConfigurationRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }

    public RequestConfiguration build(TransactionStatusRequest request) {
        return new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
    }
}
