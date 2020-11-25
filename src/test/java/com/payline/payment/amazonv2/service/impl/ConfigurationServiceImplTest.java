package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.exception.InvalidDataException;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;
import java.util.stream.Stream;

import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;

class ConfigurationServiceImplTest {
    @InjectMocks
    ConfigurationServiceImpl service = new ConfigurationServiceImpl();

    @Mock
    private ReleaseProperties releaseProperties;

    @Mock
    private ClientUtils client;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Set of locales to test the getParameters() method. ZZ allows to search in the default messages.properties file.
     */
    static Stream<Locale> getLocales() {
        return Stream.of(Locale.FRENCH, Locale.ENGLISH, new Locale("BAD_LOCALE"));
    }


    @ParameterizedTest
    @MethodSource("getLocales")
    void getParameters(Locale locale) {
        List<AbstractParameter> parameters = service.getParameters(locale);

        Assertions.assertEquals(6, parameters.size());

        for (AbstractParameter p : parameters) {
            // each parameter should have a label and a description
            assertNotNull(p.getLabel());
            assertFalse(p.getLabel().contains("???"));
            assertNotNull(p.getDescription());
            assertFalse(p.getDescription().contains("???"));

            // in case of a ListBoxParameter, it should have at least 1 value
            if (p instanceof ListBoxParameter) {
                assertFalse(((ListBoxParameter) p).getList().isEmpty());
            }
        }
    }

    @Test
    void checkNominal() {
        Mockito.doNothing().when(client).init(any());
        Mockito.doReturn(true).when(client).isPublicKeyIdOk();

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    void checkInvalidPublicKey() {
        Mockito.doNothing().when(client).init(any());
        Mockito.doReturn(false).when(client).isPublicKeyIdOk();

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(ContractConfigurationKeys.PUBLIC_KEY_ID));
    }

    @Test
    void check_emptyAccountInfo() {
        // given: an empty accountInfo
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(new HashMap<>())
                .build();

        // when: checking the configuration
        Map<String, String> errors = service.check(checkRequest);

        // then: there is an error for each parameter, each error has a valid message and authorize methods are never called
        assertEquals(5, errors.size());
        for (Map.Entry<String, String> error : errors.entrySet()) {
            assertNotNull(error.getValue());
            assertFalse(error.getValue().contains("???"));
        }

        Mockito.verify(client, never()).init(any());
    }

    @Test
    void checkPluginException(){
        Mockito.doThrow(new InvalidDataException("foo")).when(client).init(any());

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("foo", errors.get(GENERIC_ERROR));
    }

    @Test
    void checkRuntimeException(){
        Mockito.doThrow(new NullPointerException("foo")).when(client).init(any());

        ContractParametersCheckRequest request = MockUtils.aContractParametersCheckRequestBuilder().build();
        Map<String, String> errors = service.check(request);

        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("foo", errors.get(GENERIC_ERROR));
    }

    @Test
    void getReleaseInformation() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String version = "M.m.p";

        // given: the release properties are OK
        doReturn(version).when(releaseProperties).get("release.version");
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.AUGUST, 19);
        doReturn(formatter.format(cal.getTime())).when(releaseProperties).get("release.date");

        // when: calling the method getReleaseInformation
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: releaseInformation contains the right values
        assertEquals(version, releaseInformation.getVersion());
        assertEquals(2019, releaseInformation.getDate().getYear());
        assertEquals(Month.AUGUST, releaseInformation.getDate().getMonth());
        assertEquals(19, releaseInformation.getDate().getDayOfMonth());
    }
}