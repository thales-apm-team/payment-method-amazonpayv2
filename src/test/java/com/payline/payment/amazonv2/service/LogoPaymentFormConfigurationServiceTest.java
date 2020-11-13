package com.payline.payment.amazonv2.service;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.utils.i18n.I18nService;
import com.payline.payment.amazonv2.utils.properties.ConfigProperties;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

class LogoPaymentFormConfigurationServiceTest {

    /**
     * Private class used to test abstract class {@link LogoPaymentFormConfigurationService}.
     */
    private static class TestService extends LogoPaymentFormConfigurationService {
        @Override
        public PaymentFormConfigurationResponse getPaymentFormConfiguration(PaymentFormConfigurationRequest paymentFormConfigurationRequest) {
            return null;
        }
    }

    @InjectMocks
    private TestService testService;

    @Mock
    private I18nService i18n;
    @Mock
    private ConfigProperties config;

    private final Locale locale = Locale.getDefault();

    @BeforeEach
    void setup() {
        testService = new TestService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getPaymentFormLogo_nominal() {
        // given: the configuration is correct
        PaymentFormLogoRequest paymentFormLogoRequest = MockUtils.aPaymentFormLogoRequest();
        doReturn("64").when(config).get("logo.height");
        doReturn("64").when(config).get("logo.width");
        doReturn("amazonv2").when(i18n).getMessage("paymentMethod.name", paymentFormLogoRequest.getLocale());

        // when: calling method getPaymentFormLogo()
        PaymentFormLogoResponse logoResponse = testService.getPaymentFormLogo(paymentFormLogoRequest);

        // then:
        assertTrue(logoResponse instanceof PaymentFormLogoResponseFile);
        assertEquals(64, ((PaymentFormLogoResponseFile) logoResponse).getHeight());
        assertEquals(64, ((PaymentFormLogoResponseFile) logoResponse).getWidth());
        assertTrue(((PaymentFormLogoResponseFile) logoResponse).getTitle().contains("amazonv2"));
        assertTrue(((PaymentFormLogoResponseFile) logoResponse).getAlt().contains("amazonv2"));
    }

    @Test
    void getPaymentFormLogo_wrongHeight() {
        // given: the logo.height config value is incorrect (not an integer)
        PaymentFormLogoRequest paymentFormLogoRequest = MockUtils.aPaymentFormLogoRequest();
        doReturn("abc").when(config).get("logo.height");
        doReturn("64").when(config).get("logo.width");
        doReturn("amazonv2").when(i18n).getMessage("paymentMethod.name", paymentFormLogoRequest.getLocale());

        // when: calling method getPaymentFormLogo()
        assertThrows(PluginException.class, () -> testService.getPaymentFormLogo(paymentFormLogoRequest));
    }

    @Test
    void getPaymentFormLogo_wrongWidth() {
        // given: the logo.height config value is incorrect (not an integer)
        PaymentFormLogoRequest paymentFormLogoRequest = MockUtils.aPaymentFormLogoRequest();
        doReturn("64").when(config).get("logo.height");
        doReturn("abc").when(config).get("logo.width");
        doReturn("amazonv2").when(i18n).getMessage("paymentMethod.name", paymentFormLogoRequest.getLocale());

        // when: calling method getPaymentFormLogo()
        try {
            testService.getPaymentFormLogo(paymentFormLogoRequest);
            Assertions.fail("should be an PluginException");
        } catch (PluginException e) {
            Assertions.assertEquals("Plugin config error: logo height and width must be integers", e.getMessage());
        }
    }

    @Test
    void getLogo_nominal() {
        // given: a valid configuration
        doReturn("amazonpay-logo.png").when(config).get("logo.filename");
        doReturn("png").when(config).get("logo.format");
        doReturn("image/png").when(config).get("logo.contentType");

        // when: calling method getLogo()
        PaymentFormLogo paymentFormLogo = testService.getLogo("whatever", locale);

        // then:
        assertNotNull(paymentFormLogo.getContentType());
        assertNotNull(paymentFormLogo.getFile());
    }

    @Test
    void getLogo_wrongFilename() {
        // given: a valid configuration
        doReturn("does_not_exist.png").when(config).get("logo.filename");
        doReturn("png").when(config).get("logo.format");
        doReturn("image/png").when(config).get("logo.contentType");

        // when: calling method getLogo(), then: an exception is thrown
        try {
            testService.getLogo("whatever", locale);
            Assertions.fail("should be an PluginException");
        } catch (PluginException e) {
            Assertions.assertEquals("Plugin error: unable to load the logo file", e.getMessage());
        }
    }

    @Test
    void getWalletLogoNominal() {
        // given: a valid configuration
        doReturn("amazonpay-logo.png").when(config).get("logoWallet.filename");
        doReturn("png").when(config).get("logoWallet.format");
        doReturn("image/png").when(config).get("logoWallet.contentType");

        //Call wallet logo service.
        PaymentFormLogo paymentFormLogo = testService.getWalletLogo("amazonv2", Locale.getDefault());

        // check
        assertEquals("image/png", paymentFormLogo.getContentType());
        assertNotNull(paymentFormLogo.getFile());
    }

    @Test
    void getWalletLogoWithWrongConfiguration() {
        // given: a invalid configuration
        doReturn("does_not_exist.png").when(config).get("logoWallet.filename");
        doReturn("png").when(config).get("logoWallet.format");
        doReturn("image/png").when(config).get("logoWallet.contentType");

        // check
        try {
            testService.getWalletLogo("SCTI_EQUENS", locale);
            Assertions.fail("should be an PluginException");
        } catch (PluginException e) {
            Assertions.assertEquals("Plugin error: unable to load the logo file", e.getMessage());
        }
    }
}
