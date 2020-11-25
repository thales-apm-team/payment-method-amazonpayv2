package com.payline.payment.amazonv2.integration;

import com.payline.payment.amazonv2.MockUtils;
import com.payline.payment.amazonv2.bean.Script;
import com.payline.payment.amazonv2.bean.nested.ButtonColor;
import com.payline.payment.amazonv2.bean.nested.Placement;
import com.payline.payment.amazonv2.bean.nested.ProductType;
import com.payline.payment.amazonv2.service.impl.*;
import com.payline.payment.amazonv2.utils.JsonService;
import com.payline.payment.amazonv2.utils.constant.ContractConfigurationKeys;
import com.payline.payment.amazonv2.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFormUpdated;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.bean.paymentform.bean.field.PaymentFormDisplayFieldText;
import com.payline.pmapi.bean.paymentform.bean.form.CustomForm;
import com.payline.pmapi.bean.paymentform.bean.form.PartnerWidgetForm;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.PaymentFormConfigurationResponse;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.service.ConfigurationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.payline.payment.amazonv2.utils.constant.PartnerConfigurationKeys.*;
import static com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect.RedirectionRequest.RequestType.GET;


class PaymentIT {

    private final ConfigurationService configurationService = new ConfigurationServiceImpl();

    private final PaymentFormConfigurationServiceImpl paymentFormConfigurationService = new PaymentFormConfigurationServiceImpl();
    private final PaymentServiceImpl paymentService = new PaymentServiceImpl();
    private final PaymentWithRedirectionServiceImpl redirectionService = new PaymentWithRedirectionServiceImpl();
    private final RefundServiceImpl refundService = new RefundServiceImpl();

    private final String testMail = System.getProperty("project.mail");
    private final String testPassword = System.getProperty("project.password");

    @Test
    void fullPaymentTest() throws Exception {
        // ConfigurationService Test
        ContractParametersCheckRequest contractParametersCheckRequest = createContractParametersCheckRequest();
        Map<String, String> errors = configurationService.check(contractParametersCheckRequest);
        Assertions.assertTrue(errors.isEmpty());


        PaymentFormConfigurationRequest paymentFormConfigurationRequest = createPaymentFormConfigurationRequest();
        PaymentFormConfigurationResponse paymentFormConfigurationResponse = paymentFormConfigurationService.getPaymentFormConfiguration(paymentFormConfigurationRequest);

        // Assertions on paymentFormConfigurationResponse
        Assertions.assertEquals(PaymentFormConfigurationResponseSpecific.class, paymentFormConfigurationResponse.getClass());
        PaymentFormConfigurationResponseSpecific responseSpecific = (PaymentFormConfigurationResponseSpecific) paymentFormConfigurationResponse;

        Assertions.assertEquals("Payer avec Amazon Pay", responseSpecific.getPaymentForm().getDescription());
        Assertions.assertEquals(PartnerWidgetForm.class, responseSpecific.getPaymentForm().getClass());
        PartnerWidgetForm form = (PartnerWidgetForm) responseSpecific.getPaymentForm();

        Assertions.assertNull(form.getContainer());
        Assertions.assertNotNull(paymentFormConfigurationResponse);
        String scriptAfterImport = form.getLoadingScriptAfterImport();
        Script script = JsonService.getInstance().fromJson(scriptAfterImport.substring(44, scriptAfterImport.length() - 1), Script.class);
        Assertions.assertEquals(ButtonColor.Gold, script.getButtonColor());
        Assertions.assertEquals("fr_FR", script.getCheckoutLanguage());
        Assertions.assertEquals("EUR", script.getLedgerCurrency());
        Assertions.assertEquals(System.getProperty("project.merchantId"), script.getMerchantId());
        Assertions.assertEquals(Placement.Cart, script.getPlacement());
        Assertions.assertEquals(ProductType.PayOnly, script.getProductType());
        Assertions.assertTrue(script.isSandbox());

        Assertions.assertEquals(new URL("https://static-eu.payments-amazon.com/checkout.js"), form.getScriptImport().getUrl());

        // redirection
        String checkoutSessionId = this.redirect(form.getScriptImport().getUrl(), scriptAfterImport);

        // redirectionService call
        RedirectionPaymentRequest redirectionPaymentRequest = createRedirectionPaymentRequest(checkoutSessionId, null);
        PaymentResponse redirectionServicePaymentResponse = redirectionService.finalizeRedirectionPayment(redirectionPaymentRequest);

        // Assertions on redirectionServicePaymentResponse
        Assertions.assertNotNull(redirectionServicePaymentResponse);
        Assertions.assertEquals(PaymentResponseFormUpdated.class, redirectionServicePaymentResponse.getClass());
        PaymentResponseFormUpdated responseFormUpdated = (PaymentResponseFormUpdated) redirectionServicePaymentResponse;

        Assertions.assertEquals(testMail, responseFormUpdated.getRequestContext().getRequestData().get(RequestContextKeys.EMAIL));

        Assertions.assertEquals(PaymentFormConfigurationResponseSpecific.class, responseFormUpdated.getPaymentFormConfigurationResponse().getClass());
        PaymentFormConfigurationResponseSpecific redirectionResponseSpecific = (PaymentFormConfigurationResponseSpecific) responseFormUpdated.getPaymentFormConfigurationResponse();

        Assertions.assertEquals(CustomForm.class, redirectionResponseSpecific.getPaymentForm().getClass());
        CustomForm customForm = (CustomForm) redirectionResponseSpecific.getPaymentForm();
        Assertions.assertEquals("Récapitulatif de la commande", customForm.getDescription());
        Assertions.assertEquals("Confirmer", customForm.getButtonText());

        Assertions.assertEquals(3, customForm.getCustomFields().size());
        Assertions.assertEquals("Email: " + testMail, ((PaymentFormDisplayFieldText) customForm.getCustomFields().get(0)).getContent());
        Assertions.assertEquals("Nom: APM", ((PaymentFormDisplayFieldText) customForm.getCustomFields().get(1)).getContent());
        Assertions.assertEquals("Montant: 10.00€", ((PaymentFormDisplayFieldText) customForm.getCustomFields().get(2)).getContent());


        // PaymentService call
        PaymentRequest paymentRequest = createPaymentRequest(responseFormUpdated.getRequestContext());
        PaymentResponse paymentServicePaymentResponse = paymentService.paymentRequest(paymentRequest);

        // Assertions on paymentResponse
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) paymentServicePaymentResponse;
        Assertions.assertNotNull(responseRedirect.getRequestContext());
        Assertions.assertNotNull(responseRedirect.getRequestContext().getRequestData());
        Assertions.assertNotNull(responseRedirect.getRequestContext().getRequestData().get(RequestContextKeys.STEP));
        Assertions.assertEquals(RequestContextKeys.STEP_COMPLETE, responseRedirect.getRequestContext().getRequestData().get(RequestContextKeys.STEP));
        Assertions.assertNotNull(responseRedirect.getRedirectionRequest());
        Assertions.assertEquals(checkoutSessionId, responseRedirect.getPartnerTransactionId());
        Assertions.assertEquals(GET, responseRedirect.getRedirectionRequest().getRequestType());


        // redirection
        String checkoutSessionId2 = this.redirect2(responseRedirect.getRedirectionRequest().getUrl());
        Assertions.assertEquals(checkoutSessionId, checkoutSessionId2);

        // redirection call (2)
        RedirectionPaymentRequest redirectionPaymentRequest2 = createRedirectionPaymentRequest(checkoutSessionId, responseRedirect.getRequestContext());
        PaymentResponse redirectionServicePaymentResponse2 = redirectionService.finalizeRedirectionPayment(redirectionPaymentRequest2);

        // Assertions on redirectionServicePaymentResponse2
        Assertions.assertEquals(PaymentResponseSuccess.class, redirectionServicePaymentResponse2.getClass());

        PaymentResponseSuccess responseSuccess = (PaymentResponseSuccess) redirectionServicePaymentResponse2;
        Assertions.assertEquals("Completed", responseSuccess.getStatusCode());
        Assertions.assertNotNull(responseSuccess.getPartnerTransactionId());
        Assertions.assertNotEquals(checkoutSessionId2, responseSuccess.getPartnerTransactionId());
        Assertions.assertEquals(Email.class, responseSuccess.getTransactionDetails().getClass());
        Email email = (Email) responseSuccess.getTransactionDetails();
        Assertions.assertEquals(testMail, email.getEmail());


        // refund service call
        RefundRequest refundRequest = createRefundRequest(responseSuccess.getPartnerTransactionId(), responseFormUpdated.getRequestContext());
        RefundResponse refundResponse = refundService.refundRequest(refundRequest);

        // Assertions on refund response;
        Assertions.assertNotNull(refundResponse);
        Assertions.assertEquals(RefundResponseSuccess.class, refundResponse.getClass());
        RefundResponseSuccess refundResponseSuccess = (RefundResponseSuccess) refundResponse;
        Assertions.assertTrue(refundResponseSuccess.getPartnerTransactionId().startsWith("S"));
        Assertions.assertEquals("PENDING", refundResponseSuccess.getStatusCode());

    }

    private String redirect(URL url, String script) {
        String template = "<html><body>\n" +
                "<div id=\"AmazonPayButton\"></div>\n" +
                "<script src=\"[remoteScript]\"></script>\n" +
                "<script type=\"text/javascript\" charset=\"utf-8\">[command]</script>\n" +
                "</body></html>";
        String page = template.replace("[remoteScript]", url.toString())
                .replace("[command]", script);

        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        String amazonPayCheckoutSessionId = "0";
        try {
            // page creation
            String filename = "src/test/resources/index.html";
            File index = new File(filename);
            index.createNewFile();

            FileWriter writer = new FileWriter(index);
            writer.write(page);
            writer.close();

            String s = System.getProperty("user.dir") + "/src/test/resources/index.html";
            driver.get("file:" + s);

            // login proccess
            driver.findElement(By.id("AmazonPayButton")).click();
            login(driver);

            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("a-autoid-0-announce")));
            driver.findElement(By.xpath("//*[@id=\"a-autoid-0\"]/span/input")).click();

            // https://www.redirection.url.com/?amazonCheckoutSessionId=ebdeac2f-1505-4294-bd0b-106b5990390a
            wait.until(ExpectedConditions.urlContains("https://www.redirection.url.com"));
            amazonPayCheckoutSessionId = driver.getCurrentUrl().split("=")[1];

            index.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return amazonPayCheckoutSessionId;
    }

    private String redirect2(URL url) {
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        String amazonPayCheckoutSessionId = "";

        try {
            driver.get(url.toString());
            login(driver);
            WebDriverWait wait = new WebDriverWait(driver, 30);

            wait.until(ExpectedConditions.urlContains("https://www.redirection.url.com"));
            amazonPayCheckoutSessionId = driver.getCurrentUrl().split("=")[1];
        } finally {
            driver.quit();
        }
        return amazonPayCheckoutSessionId;
    }
    
    private void login(WebDriver driver){
        driver.findElement(By.id("ap_email")).sendKeys(testMail);
        driver.findElement(By.id("ap_password")).sendKeys(testPassword);
        driver.findElement(By.id("signInSubmit")).click();
    }

    private ContractConfiguration createContractConfiguration() {
        Map<String, ContractProperty> contractProperties = new HashMap<>();
        contractProperties.put(ContractConfigurationKeys.MERCHANT_ID, new ContractProperty(System.getProperty("project.merchantId")));
        contractProperties.put(ContractConfigurationKeys.MERCHANT_NAME, new ContractProperty(System.getProperty("project.merchantName")));
        contractProperties.put(ContractConfigurationKeys.STORE_ID, new ContractProperty(System.getProperty("project.storeId")));
        contractProperties.put(ContractConfigurationKeys.PUBLIC_KEY_ID, new ContractProperty(System.getProperty("project.publicKeyId")));
        contractProperties.put(ContractConfigurationKeys.BUTTON_COLOR, new ContractProperty(ButtonColor.Gold.name()));
        contractProperties.put(ContractConfigurationKeys.PRODUCT_TYPE, new ContractProperty(ProductType.PayOnly.name()));

        return new ContractConfiguration("AmazonPayV2", contractProperties);
    }

    private PartnerConfiguration createPartnerConfiguration() throws Exception {
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(AMAZON_SCRIPT_URL, "https://static-eu.payments-amazon.com/checkout.js");
        partnerConfigurationMap.put(PLACEMENT, Placement.Cart.name());
        Map<String, String> sensitivePartnerConfigurationMap = new HashMap<>();
        sensitivePartnerConfigurationMap.put(PRIVATE_KEY, new String(Files.readAllBytes(Paths.get(System.getProperty("project.privateKey")))));

        return new PartnerConfiguration(partnerConfigurationMap, sensitivePartnerConfigurationMap);
    }

    private ContractParametersCheckRequest createContractParametersCheckRequest() throws Exception {
        return ContractParametersCheckRequest.CheckRequestBuilder.aCheckRequest()
                .withAccountInfo(MockUtils.anAccountInfo(createContractConfiguration()))
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withLocale(Locale.FRANCE)
                .withPartnerConfiguration(createPartnerConfiguration())
                .build();
    }

    private PaymentFormConfigurationRequest createPaymentFormConfigurationRequest() throws Exception {
        return PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder
                .aPaymentFormConfigurationRequest()
                .withContractConfiguration(createContractConfiguration())
                .withPartnerConfiguration(createPartnerConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withLocale(Locale.FRANCE)
                .withOrder(MockUtils.aPaylineOrder())
                .withAmount(MockUtils.aPaylineAmount())
                .withBuyer(MockUtils.aBuyer())
                .build();
    }

    private RedirectionPaymentRequest createRedirectionPaymentRequest(String checkoutSessionId, RequestContext context) throws Exception {
        Map<String, String[]> httpRequestParameters = new HashMap<>();
        httpRequestParameters.put("AmazonCheckoutSessionId", new String[]{checkoutSessionId});

        return RedirectionPaymentRequest.builder()
                .withHttpRequestParametersMap(httpRequestParameters)
                .withRequestContext(context)
                .withContractConfiguration(createContractConfiguration())
                .withPartnerConfiguration(createPartnerConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withLocale(Locale.FRANCE)
                .withOrder(MockUtils.aPaylineOrder())
                .withAmount(MockUtils.aPaylineAmount())
                .withBuyer(MockUtils.aBuyer())
                .withBrowser(MockUtils.aBrowser())
                .withTransactionId("123456")
                .build();

    }

    private PaymentRequest createPaymentRequest(RequestContext context) throws Exception {
        return PaymentRequest.builder()
                .withTransactionId("123456")
                .withCaptureNow(true)
                .withRequestContext(context)
                .withContractConfiguration(createContractConfiguration())
                .withPartnerConfiguration(createPartnerConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withLocale(Locale.FRANCE)
                .withOrder(MockUtils.aPaylineOrder())
                .withAmount(MockUtils.aPaylineAmount())
                .withBuyer(MockUtils.aBuyer())
                .withBrowser(MockUtils.aBrowser())
                .build();
    }

    private RefundRequest createRefundRequest(String partnerTransactionId,RequestContext context) throws Exception {
        return RefundRequest.RefundRequestBuilder
                .aRefundRequest()
                .withTransactionId("12345")
                .withPartnerTransactionId(partnerTransactionId)
                .withRequestContext(context)
                .withContractConfiguration(createContractConfiguration())
                .withPartnerConfiguration(createPartnerConfiguration())
                .withEnvironment(MockUtils.anEnvironment())
                .withOrder(MockUtils.aPaylineOrder())
                .withAmount(MockUtils.aPaylineAmount())
                .withBuyer(MockUtils.aBuyer())
                .build();
    }
}
