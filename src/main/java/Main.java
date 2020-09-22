import com.amazon.pay.api.AmazonPayClient;
import com.amazon.pay.api.AmazonPayResponse;
import com.amazon.pay.api.PayConfiguration;
import com.amazon.pay.api.WebstoreClient;
import com.amazon.pay.api.types.Environment;
import com.amazon.pay.api.types.Region;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.bean.Refund;
import com.payline.payment.amazonv2.bean.Script;
import com.payline.payment.amazonv2.bean.nested.*;
import com.payline.payment.amazonv2.util.JsonService;

public class Main {
    static String merchantId = "A371RBM9TIPUSO";

    static String storeId = "amzn1.application-oa2-client.67a58ba5a6f444dd94c7b2301404b1b4";
    static String checkoutSessionId = "0734320a-5bb9-4537-8e41-a1730ac46af9";


    static String publicKeyId = "AHXUC3H5NEWPB7PY6KDKQ53O";
    static String privateKey = "";

    static JsonService jsonService = JsonService.getInstance();

    public static void main(String[] args) throws Exception {
// STEP 0: init client
        PayConfiguration payConfiguration = new PayConfiguration()
                .setRegion(Region.EU)
                .setPublicKeyId(publicKeyId)
                .setPrivateKey(privateKey)
                .setEnvironment(Environment.SANDBOX);
        WebstoreClient webstoreClient = new WebstoreClient(payConfiguration);
        AmazonPayClient client = new AmazonPayClient(payConfiguration);


// STEP 1: Script to load in the JS
        System.out.println("//////////////////////////////// STEP 1 : Creation SCRIPT ////////////////////////////////");
        // create Script to load in the JS
        CheckoutSession checkoutSession = createInitPayload();
        String initSessionString = jsonService.toJson(checkoutSession);

        // generate signature
        String signature = client.generateButtonSignature(jsonService.toJSONObject(initSessionString));

        // generate Script
        Script script = createScript(initSessionString, signature);
        System.out.println(jsonService.toJson(script));

// STEP 2: Get the transaction status via "getCheckoutSession"
        System.out.println("//////////////////////////////// STEP 2: GET TRANSACTION ////////////////////////////////");

        // use APV2 library to call for getting checkoutSession status
        AmazonPayResponse response = webstoreClient.getCheckoutSession(checkoutSessionId);
        System.out.println(response.getRawResponse());
        checkoutSession = jsonService.fromJson(response.getRawResponse(), CheckoutSession.class);

// STEP 3: update transaction via updateCheckoutSession
        System.out.println("//////////////////////////////// STEP 3: UPDATE TRANSACTION ////////////////////////////////");

        // create checkoutSession used to update
        checkoutSession = createUpdatePayload();
        String updateCheckoutSessionString = jsonService.toJson(checkoutSession);

        // print checkoutSession used to update
        System.out.println(updateCheckoutSessionString);

        // use APV2 library to call for update checkoutSession
        response = webstoreClient.updateCheckoutSession(checkoutSessionId, jsonService.toJSONObject(updateCheckoutSessionString));
        System.out.println(response.getRawResponse());
        checkoutSession = jsonService.fromJson(response.getRawResponse(), CheckoutSession.class);

// STEP 4: complete the transaction
        System.out.println("//////////////////////////////// STEP 4: COMPLETE TRANSACTION ////////////////////////////////");

        // create paymentDetails used to complete
        PaymentDetails paymentDetails = createCompletePayload();
        String completeCheckoutSessionString = jsonService.toJson(paymentDetails);

        // print paymentDetails used to complete
        System.out.println(completeCheckoutSessionString);

        // use APV2 library to call for complete checkoutSession
        response = webstoreClient.completeCheckoutSession(checkoutSessionId, jsonService.toJSONObject(completeCheckoutSessionString));
        System.out.println(response.getRawResponse());
        checkoutSession = jsonService.fromJson(response.getRawResponse(), CheckoutSession.class);
        String chargeId = checkoutSession.getChargeId();


// STEP 5: create refund
        System.out.println("//////////////////////////////// STEP 5: CREATE REFUND ////////////////////////////////");

        // create refund object used to refund
        Refund refund = createRefundPayload(chargeId);

        // print refund object used to refund
        String refundString = jsonService.toJson(refund);
        System.out.println(refundString);

        // use APV2 library to call for a refund
        response = webstoreClient.createRefund(jsonService.toJSONObject(refundString));
        System.out.println(response.getRawResponse());
        refund = jsonService.fromJson(response.getRawResponse(), Refund.class);
        String refundId = refund.getRefundId();

// STEP 6: get refund status
        System.out.println("//////////////////////////////// STEP 6: GET REFUND ////////////////////////////////");
        response = webstoreClient.getRefund(refundId);
        System.out.println(response.getRawResponse());

    }

    private static CheckoutSession createInitPayload() {
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .paymentIntent(PaymentDetails.PaymentIntent.AuthorizeWithCapture)
                .canHandlePendingAuthorization(false)
                .softDescriptor("soft descriptor")
                .chargeAmount(Price.builder()
                        .amount("1")
                        .currencyCode("EUR")
                        .build())
                .build();

        return CheckoutSession.builder()
                .webCheckoutDetails(WebCheckoutDetails.builder()
                        .checkoutReviewReturnUrl("https://localhost/store/checkout_review")
                        .checkoutResultReturnUrl("https://google.com")
                        .build())
                .storeId(storeId)
                .paymentDetails(paymentDetails)
                .build();
    }

    private static Script createScript(String session, String signature) {
        CreateCheckoutSessionConfig sessionConfig = CreateCheckoutSessionConfig.builder()
                .payloadJSON(session)
                .publicKeyId("AHXUC3H5NEWPB7PY6KDKQ53O")
                .signature(signature)
                .build();

        return Script.builder()
                .merchantId("A371RBM9TIPUSO")
                .ledgerCurrency("EUR")
                .sandbox(true)
                .checkoutLanguage("en_GB")
                .productType(ProductType.PayAndShip)
                .placement(Placement.Cart)
                .buttonColor(ButtonColor.Gold)
                .createCheckoutSessionConfig(sessionConfig)
                .build();


    }

    private static CheckoutSession createUpdatePayload() {
        WebCheckoutDetails webCheckoutDetails = WebCheckoutDetails.builder()
                .checkoutResultReturnUrl("https://localhost/store/checkout_review")
                .build();

        PaymentDetails paymentDetails = PaymentDetails.builder()
                .paymentIntent(PaymentDetails.PaymentIntent.AuthorizeWithCapture)
                .canHandlePendingAuthorization(false)
                .softDescriptor("soft descriptor")
                .chargeAmount(Price.builder()
                        .amount("1")
                        .currencyCode("EUR")
                        .build())
                .build();

        MerchantMetadata merchantMetaData = MerchantMetadata.builder()
                .merchantReferenceId("1122334455")
                .merchantStoreName("totoShop")
                .noteToBuyer("123456789")
                .build();

        return CheckoutSession.builder()
                .webCheckoutDetails(webCheckoutDetails)
                .paymentDetails(paymentDetails)
                .merchantMetaData(merchantMetaData)
                .build();
    }


    private static PaymentDetails createCompletePayload() {
        Price chargeAmount = Price.builder()
                .amount("1")
                .currencyCode("EUR")
                .build();

        return PaymentDetails.builder()
                .chargeAmount(chargeAmount)
                .build();
    }

    private static Refund createRefundPayload(String chargeId) {
        return Refund.builder()
                .chargeId(chargeId)
                .refundAmount(Price.builder()
                        .amount("1")
                        .currencyCode("EUR").build())
                .softDescriptor("soft descriptor")
                .build();
    }

}