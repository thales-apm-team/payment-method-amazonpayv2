package com.payline.payment.amazonv2.bean.configuration;

import com.payline.payment.amazonv2.exception.InvalidDataException;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.Environment;
import lombok.Value;

/**
 * Generic class that supports any type of request's configuration.
 */
@Value
public class RequestConfiguration {

    ContractConfiguration contractConfiguration;
    Environment environment;
    PartnerConfiguration partnerConfiguration;

    public RequestConfiguration(ContractConfiguration contractConfiguration, Environment environment, PartnerConfiguration partnerConfiguration) {
        if (contractConfiguration == null) {
            throw new InvalidDataException("Missing request contractConfiguration");
        }
        if (environment == null) {
            throw new InvalidDataException("Missing request environment");
        }
        if (partnerConfiguration == null) {
            throw new InvalidDataException("Missing request partnerConfiguration");
        }
        this.contractConfiguration = contractConfiguration;
        this.environment = environment;
        this.partnerConfiguration = partnerConfiguration;
    }
}
