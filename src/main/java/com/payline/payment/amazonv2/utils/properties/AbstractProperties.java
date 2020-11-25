package com.payline.payment.amazonv2.utils.properties;

import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.PluginUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractProperties {

    private final Properties properties;

    protected AbstractProperties() {
        properties = new Properties();
        readProperties();
    }

    /**
     * Get a configuration property by its name.
     * Warning, if the property is environment-dependent, use partnerConfiguration instead.
     *
     * @param key The name/key of the property to get.
     * @return The property value. Can be null if the property has not been found.
     */
    public String get(final String key) {
        return this.properties.getProperty(key);
    }

    /**
     * Read the properties files using the filename returned by the method getFilename().
     */
    void readProperties() {
        String filename = getFilename();
        if (PluginUtils.isEmpty(filename)) {
            throw new PluginException("Properties filename must not be null or empty");
        }

        try(InputStream inputStream = AbstractProperties.class.getClassLoader().getResourceAsStream(filename)){
            if( inputStream == null ){
                throw new PluginException("Cannot find properties file: " + filename);
            }
            this.properties.load(inputStream);
        } catch (IOException e) {
            throw new PluginException("Unable to load properties files: "+filename, e);
        }
    }

    /**
     * Get the properties file's name
     *
     * @return the properties file's name
     */
    abstract String getFilename();

}