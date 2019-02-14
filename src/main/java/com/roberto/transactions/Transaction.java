package com.roberto.transactions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
