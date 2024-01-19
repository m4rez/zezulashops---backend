package com.organica.controllers;

import cz.gopay.api.v3.GPClientException;
import cz.gopay.api.v3.IGPConnector;
import cz.gopay.api.v3.impl.apacheclient.HttpClientGPConnector;
import cz.gopay.api.v3.model.access.OAuth;
import cz.gopay.api.v3.model.common.Currency;
import cz.gopay.api.v3.model.payment.*;
import cz.gopay.api.v3.model.payment.support.Payer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Scanner;


@RestController
@CrossOrigin
@RequestMapping("")
public class GoPayController {

    IGPConnector connector;

    @Value("${gopay.api.id}")
    private String id;
    @Value("${gopay.api.secret}")
    private String secret;


    @GetMapping("/getCredentials")
    public IGPConnector getCredentials() throws GPClientException {
        IGPConnector connector = HttpClientGPConnector.build("https://gw.sandbox.gopay.com/api");
        connector.getAppToken(id, secret).getAccessToken().getAccessToken();
        return connector;
    }
    @GetMapping("/getCredentialss")
    public IGPConnector getFullCredentials() throws GPClientException {
        IGPConnector connector = HttpClientGPConnector.build("https://gw.sandbox.gopay.com/api");
        connector.getAppToken(id, secret, OAuth.SCOPE_PAYMENT_ALL).getAccessToken().getAccessToken();
        return connector;
    }

    @GetMapping("/pay")
    public void pay() throws GPClientException {

        IGPConnector connector = getFullCredentials();
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
            ;
        } catch (GPClientException e) {
            System.out.println("nepovedlo");
        }
    }

    @GetMapping("refund")
    public void refund() throws GPClientException{

        PaymentResult result = getFullCredentials().refundPayment(3220631869L, 5L);
        System.out.println(result.getResult());
    }
    @GetMapping("check")
    public void check() throws GPClientException{
        IGPConnector connector = HttpClientGPConnector.build("https://gw.sandbox.gopay.com/api");
        connector.getAppToken(id, secret).getAccessToken().getAccessToken();
        System.out.println(connector.paymentStatus(3220631869L));
    }
}