package com.organica.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.gopay.api.v3.model.payment.support.Payer;
import cz.gopay.api.v3.model.payment.support.Target;

public class PaymentResponse {

    private long id;
    @JsonProperty("order_number")
    private String orderNumber;
    private String state;
    private int amount;
    private String currency;
    private Payer payer;
    private Target target;
    @JsonProperty("gw_url")
    private String gwUrl;

    // Getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public String getGwUrl() {
        return gwUrl;
    }

    public void setGwUrl(String gwUrl) {
        this.gwUrl = gwUrl;
    }
}

