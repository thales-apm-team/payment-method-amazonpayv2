package com.payline.payment.amazonv2.service;

import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.i18n.I18nService;
import com.payline.payment.amazonv2.utils.properties.ConfigProperties;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import com.payline.pmapi.service.PaymentFormConfigurationService;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
@Log4j2
public abstract class LogoPaymentFormConfigurationService implements PaymentFormConfigurationService {

    protected I18nService i18n = I18nService.getInstance();
    protected ConfigProperties config = ConfigProperties.getInstance();

    @Override
    public PaymentFormLogoResponse getPaymentFormLogo(final PaymentFormLogoRequest paymentFormLogoRequest) {
        Locale locale = paymentFormLogoRequest.getLocale();
        int height;
        int width;
        try {
            height = Integer.parseInt(config.get("logo.height"));
            width = Integer.parseInt(config.get("logo.width"));
        } catch (NumberFormatException e) {
            throw new PluginException("Plugin config error: logo height and width must be integers", e);
        }

        return PaymentFormLogoResponseFile.PaymentFormLogoResponseFileBuilder.aPaymentFormLogoResponseFile()
                .withHeight(height)
                .withWidth(width)
                .withTitle(i18n.getMessage("paymentMethod.name", locale))
                .withAlt(i18n.getMessage("paymentMethod.name", locale) + " logo")
                .build();
    }

    @Override
    public PaymentFormLogo getLogo(final String paymentMethodIdentifier, final Locale locale) {
        final String filename = config.get("logo.filename");
        final String format = config.get("logo.format");
        final String contentType = config.get("logo.contentType");
        return getLogoByFilename(filename, format, contentType);
    }

    @Override
    public PaymentFormLogo getWalletLogo(final String paymentMethodIdentifier, final Locale locale) {
        final String filename = config.get("logoWallet.filename");
        final String format = config.get("logoWallet.format");
        final String contentType = config.get("logoWallet.contentType");
        return getLogoByFilename(filename, format, contentType);
    }

    private PaymentFormLogo getLogoByFilename(final String filename, final String format, final String contentType) {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                log.error("Unable to load file {}", filename);
                throw new PluginException("Plugin error: unable to load the logo file");
            }
            // Read logo file
            final BufferedImage logo = ImageIO.read(input);

            // Recover byte array from image
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(logo, format, baos);

            return PaymentFormLogo.PaymentFormLogoBuilder.aPaymentFormLogo()
                    .withFile(baos.toByteArray())
                    .withContentType(contentType)
                    .build();
        } catch (final IOException e) {
            throw new PluginException("Plugin error: unable to read the logo", e);
        }
    }

}
