package com.payline.payment.amazonv2.utils.i18n;

import com.payline.payment.amazonv2.utils.properties.ConfigProperties;
import lombok.extern.log4j.Log4j2;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * I18n (for Internationalization) service that provides messages following a given locale.
 */
@Log4j2
public class I18nService {

    private static final String DEFAULT_LOCALE = "en";

    private ConfigProperties configProperties = ConfigProperties.getInstance();

    I18nService() {
        String defaultLocale = configProperties.get("i18n.defaultLocale");
        Locale.setDefault( new Locale(defaultLocale != null ? defaultLocale : DEFAULT_LOCALE) );
    }

    private static class SingletonHolder {
        private static final I18nService instance = new I18nService();
    }

    public static I18nService getInstance() {
        return SingletonHolder.instance;
    }

    /**
     * Retrieve the message identified by the given key in the language of the given locale.
     *
     * @param key The identifying key of the message
     * @param locale The locale
     * @return The message in the right language
     */
    public String getMessage(final String key, final Locale locale) {
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        try {
            return messages.getString(key);
        }
        catch (MissingResourceException e) {
            log.error("Trying to get a message with a key that does not exist: {} (language: {})", key, locale.getLanguage());
            return "???" + locale + "." + key + "???";
        }
    }
}