package com.payline.payment.amazonv2.utils.properties;

/**
 * Utility class which reads and provides release properties.
 */
public class ReleaseProperties extends AbstractProperties {

    private static final String FILENAME = "release.properties";

    ReleaseProperties(){}

    private static class Holder {
        private static final ReleaseProperties instance = new ReleaseProperties();
    }

    public static ReleaseProperties getInstance(){
        return Holder.instance;
    }

    @Override
    protected String getFilename() {
        return FILENAME;
    }

}
