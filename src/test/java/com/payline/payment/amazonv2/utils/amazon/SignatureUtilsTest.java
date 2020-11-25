package com.payline.payment.amazonv2.utils.amazon;

import com.amazon.pay.api.AmazonPayClient;
import com.amazon.pay.api.exceptions.AmazonPayClientException;
import com.payline.payment.amazonv2.bean.CheckoutSession;
import com.payline.payment.amazonv2.exception.PluginException;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;

class SignatureUtilsTest {
    @InjectMocks
    SignatureUtils signatureUtils = SignatureUtils.getInstance();

    @Mock
    AmazonPayClient client;

    CheckoutSession session = CheckoutSession.builder().build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void generateSignatureNominal() throws Exception {
        Mockito.doReturn("thisIsASignature").when(client).generateButtonSignature(any(JSONObject.class));

        String signature = signatureUtils.generateSignature(session);
        Assertions.assertEquals("thisIsASignature", signature);
    }

    @Test
    void generateSignatureException() throws Exception {
        Mockito.doThrow(new AmazonPayClientException("foo")).when(client).generateButtonSignature(any(JSONObject.class));

        PluginException e = Assertions.assertThrows(PluginException.class, () -> signatureUtils.generateSignature(session));
        Assertions.assertEquals("Unable to generate signature", e.getErrorCode());
    }
}