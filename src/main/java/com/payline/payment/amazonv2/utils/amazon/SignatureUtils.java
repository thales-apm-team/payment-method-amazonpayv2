package com.payline.payment.amazonv2.utils.amazon;

import com.amazon.pay.api.AmazonPayClient;
import com.amazon.pay.api.exceptions.AmazonPayClientException;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.pmapi.logger.LogManager;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.Logger;

public class SignatureUtils extends ConfigurationUtils {
    private static final Logger LOGGER = LogManager.getLogger(SignatureUtils.class);

    private AmazonPayClient client;

    private static class Holder {
        private static final SignatureUtils instance = new SignatureUtils();
    }

    public static SignatureUtils getInstance() {
        return Holder.instance;
    }

    @Override
    public void init(RequestConfiguration configuration) {
        try {
            super.init(configuration);
            client = new AmazonPayClient(this.payConfiguration);
        } catch (AmazonPayClientException e) {
            String errorMessage = "unable to init Amazon signature utils";
            LOGGER.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }
    }


    public String generateSignature(CheckoutSession checkoutSession) {
        try {
            // convert CheckoutSession into JSONObject
            JSONObject jsonObject = jsonService.toJSONObject(jsonService.toJson(checkoutSession));

            // generate signature
            return client.generateButtonSignature(jsonObject);
        } catch (AmazonPayClientException e) {
            String errorMessage = "Unable to generate signature";
            LOGGER.error(errorMessage);
            throw new PluginException(errorMessage, e);
        }

    }

}
