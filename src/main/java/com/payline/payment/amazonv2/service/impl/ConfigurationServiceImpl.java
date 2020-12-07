package com.payline.payment.amazonv2.service.impl;

import com.payline.payment.amazonv2.bean.configuration.RequestConfiguration;
import com.payline.payment.amazonv2.bean.nested.ButtonColor;
import com.payline.payment.amazonv2.bean.nested.ProductType;
import com.payline.payment.amazonv2.exception.PluginException;
import com.payline.payment.amazonv2.service.RequestConfigurationService;
import com.payline.payment.amazonv2.utils.amazon.ClientUtils;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.i18n.I18nService;
import com.payline.payment.amazonv2.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;
@Log4j2
public class ConfigurationServiceImpl implements ConfigurationService {


    private ReleaseProperties releaseProperties = ReleaseProperties.getInstance();
    private final I18nService i18n = I18nService.getInstance();
    private ClientUtils clientUtils = ClientUtils.getInstance();

    private static final String I18N_CONTRACT_PREFIX = "contract.";


    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // merchant name
        parameters.add(newInputParameter(ContractConfigurationKeys.MERCHANT_NAME, false, locale));

        // merchant Id
        parameters.add(newInputParameter(ContractConfigurationKeys.MERCHANT_ID, true, locale));

        // store id
        parameters.add(newInputParameter(ContractConfigurationKeys.STORE_ID, true, locale));

        // public key id
        parameters.add(newInputParameter(ContractConfigurationKeys.PUBLIC_KEY_ID, true, locale));

        // button color
        Map<String, String> colors = new HashMap<>();
        colors.put(ButtonColor.Gold.name(), i18n.getMessage("color.gold", locale));
        colors.put(ButtonColor.LightGray.name(), i18n.getMessage("color.lightGray", locale));
        colors.put(ButtonColor.DarkGray.name(), i18n.getMessage("color.darkGray", locale));
        parameters.add(this.newListBoxParameter(ContractConfigurationKeys.BUTTON_COLOR, colors, ButtonColor.Gold.name(), true, locale));

        // product types
        Map<String, String> productTypes = new HashMap<>();
        productTypes.put(ProductType.PayOnly.name(), i18n.getMessage("productType.payOnly", locale));
        productTypes.put(ProductType.PayAndShip.name(), i18n.getMessage("productType.payAndShip", locale));
        parameters.add(this.newListBoxParameter(ContractConfigurationKeys.PRODUCT_TYPE, productTypes, ProductType.PayOnly.name(), true, locale));

        return parameters;
    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {
        final Map<String, String> errors = new HashMap<>();
        try {

            // check il all mandatory fields are filled
            Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();
            Locale locale = contractParametersCheckRequest.getLocale();

            // check required fields
            for (AbstractParameter param : this.getParameters(locale)) {
                if (param.isRequired() && accountInfo.get(param.getKey()) == null) {
                    log.info("contract param: {} is mandatory but missing", param.getKey());
                    String message = i18n.getMessage(I18N_CONTRACT_PREFIX + param.getKey() + ".requiredError", locale);
                    errors.put(param.getKey(), message);
                }
            }

            if (errors.size() == 0) {
                // call for a getCheckoutSession with a bad checkoutSession id
                RequestConfiguration configuration = RequestConfigurationService.getInstance().build(contractParametersCheckRequest);
                clientUtils.init(configuration);
                if (!clientUtils.isPublicKeyIdOk()) {
                    errors.put(ContractConfigurationKeys.PUBLIC_KEY_ID, i18n.getMessage(I18N_CONTRACT_PREFIX + ContractConfigurationKeys.PUBLIC_KEY_ID + ".invalid", locale));
                }
            }

        } catch (PluginException e) {
            log.info("unable to execute PaymentService#paymentRequest", e);
            errors.put(GENERIC_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Unexpected plugin error", e);
            errors.put(GENERIC_ERROR, e.getMessage());

        }

        return errors;
    }


    @Override
    public ReleaseInformation getReleaseInformation() {
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(LocalDate.parse(releaseProperties.get("release.date"), DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .withVersion(releaseProperties.get("release.version"))
                .build();
    }

    @Override
    public String getName(Locale locale) {
        return i18n.getMessage("paymentMethod.name", locale);
    }

    /**
     * Build and return a new <code>InputParameter</code> for the contract configuration.
     *
     * @param key      The parameter key
     * @param required Is this parameter required ?
     * @param locale   The current locale
     * @return The new input parameter
     */
    private InputParameter newInputParameter(String key, boolean required, Locale locale) {
        InputParameter inputParameter = new InputParameter();
        inputParameter.setKey(key);
        inputParameter.setLabel(i18n.getMessage(I18N_CONTRACT_PREFIX + key + ".label", locale));
        inputParameter.setDescription(i18n.getMessage(I18N_CONTRACT_PREFIX + key + ".description", locale));
        inputParameter.setRequired(required);
        return inputParameter;
    }

    /**
     * Build and return a new <code>ListBoxParameter</code> for the contract configuration.
     *
     * @param key          The parameter key
     * @param values       All the possible values for the list box
     * @param defaultValue The key of the default value (which will be selected by default)
     * @param required     Is this parameter required ?
     * @param locale       The current locale
     * @return The new list box parameter
     */
    private ListBoxParameter newListBoxParameter(String key, Map<String, String> values, String defaultValue, boolean required, Locale locale) {
        ListBoxParameter listBoxParameter = new ListBoxParameter();
        listBoxParameter.setKey(key);
        listBoxParameter.setLabel(i18n.getMessage(I18N_CONTRACT_PREFIX + key + ".label", locale));
        listBoxParameter.setDescription(i18n.getMessage(I18N_CONTRACT_PREFIX + key + ".description", locale));
        listBoxParameter.setList(values);
        listBoxParameter.setRequired(required);
        listBoxParameter.setValue(defaultValue);
        return listBoxParameter;
    }

}


