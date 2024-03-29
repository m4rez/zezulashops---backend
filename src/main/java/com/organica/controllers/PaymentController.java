package com.organica.controllers;


import com.organica.payload.PaymentDetails;
import com.organica.services.PaymentService;
import cz.gopay.api.v3.GPClientException;
import cz.gopay.api.v3.IGPConnector;
import cz.gopay.api.v3.impl.apacheclient.HttpClientGPConnector;
import cz.gopay.api.v3.model.access.OAuth;
import cz.gopay.api.v3.model.common.Currency;
import cz.gopay.api.v3.model.payment.BasePayment;
import cz.gopay.api.v3.model.payment.Lang;
import cz.gopay.api.v3.model.payment.Payment;
import cz.gopay.api.v3.model.payment.PaymentFactory;
import cz.gopay.api.v3.model.payment.support.Payer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Value("${gopay.api.id}")
    private String id;
    @Value("${gopay.api.secret}")
    private String secret;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/{amount}")
    public ResponseEntity<String> CreatePayment(@PathVariable Double amount) throws GPClientException{
        String gwurl = "";
        IGPConnector connector = HttpClientGPConnector.build("https://gw.sandbox.gopay.com/api");
        connector.getAppToken(id, secret, OAuth.SCOPE_PAYMENT_ALL).getAccessToken().getAccessToken();
        Payer payer = new Payer();


        BasePayment payment = PaymentFactory.createBasePaymentBuilder()
                .order("123456", 10L, Currency.CZK, "asdffd")
                .addItem("thng", 1L, 1L)
                .addAdditionalParameter("Test name", "Test value")
                .withCallback("https://example.com/your-return-url", "https://example.com/your-notify-url")
                .payer(payer)
                .inLang(Lang.CS)
                .toEshop(8569800855L)
                .build();
        try {
            Payment result = connector.createPayment(payment);
            System.out.println(result.getGwUrl());
            System.out.println(result.getState());
            System.out.println(result.getId());
            System.out.println(result.getAmount());
            System.out.println(connector.paymentStatus(result.getId()));
            gwurl = result.getGwUrl();
            ;
        } catch (GPClientException e) {
            System.out.println("nepovedlo");
        }
        return new ResponseEntity<>(gwurl, HttpStatusCode.valueOf(200));
    }

}
