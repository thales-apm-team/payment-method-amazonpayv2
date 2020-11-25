package com.payline.payment.amazonv2.utils.properties;

/**
 * Utility class which reads and provides config properties.
 */
public class ConfigProperties extends AbstractProperties {

    private static final String FILENAME = "config.properties";

    ConfigProperties() {
    }

    private static class Holder {
        private static final ConfigProperties instance = new ConfigProperties();
    }

    public static ConfigProperties getInstance() {
        return Holder.instance;
    }

    @Override
    protected String getFilename() {
        return FILENAME;
    }

}
