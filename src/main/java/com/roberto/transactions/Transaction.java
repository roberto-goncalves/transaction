package com.roberto.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Transaction {

    @JsonProperty("merchant")
    private String merchant;
    @JsonProperty("amount")
    private double amount;
    @JsonProperty("time")
    private Date time;

    public Transaction(String merchant, double amount, Date time) {
        this.merchant = merchant;
        this.amount = amount;
        this.time = time;
    }

    public Transaction(Transaction transaction) {
        this.merchant = transaction.merchant;
        this.amount = transaction.amount;
        this.time = transaction.time;
    }

    public String getMerchant() {
        return merchant;
    }

    public double getAmount() {
        return amount;
    }

    public Date getTime() {
        return time;
    }
}
