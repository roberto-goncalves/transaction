package com.roberto.transactions;

import java.util.List;

public class TransactionAuthorizationRequest {
    private Account account;
    private Transaction transaction;
    private List<Transaction> lastTransactions;

    public Account getAccount() {
        return account;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public List<Transaction> getLastTransactions() {
        return lastTransactions;
    }


}
