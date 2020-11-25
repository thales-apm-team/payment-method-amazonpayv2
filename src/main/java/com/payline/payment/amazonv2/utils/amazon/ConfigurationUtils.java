package com.payline.payment.amazonv2.utils.amazon;

import com.amazon.pay.api.PayConfiguration;
import com.amazon.pay.api.exceptions.AmazonPayClientException;
import com.amazon.pay.api.types.Environment;
import com.amazon.pay.api.types.Region;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.JsonService;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.constant.PartnerConfigurationKeys;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfigurationUtils {
    private static final Logger LOGGER = LogManager.getLogger(ConfigurationUtils.class);

    protected PayConfiguration payConfiguration;
    protected final JsonService jsonService = JsonService.getInstance();

    ConfigurationUtils() {
    }

    void init(RequestConfiguration configuration) {
        try {
            payConfiguration = new PayConfiguration()
                    .setRegion(Region.EU)
                    .setPublicKeyId(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.PUBLIC_KEY_ID).getValue())
                    .setPrivateKey(configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.PRIVATE_KEY))
                    .setEnvironment(configuration.getEnvironment().isSandbox() ? Environment.SANDBOX : Environment.LIVE);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to init Amazon configuration";
            LOGGER.error(errorMessage, e);
            throw new PluginException(errorMessage, e);
        }
    }

}
