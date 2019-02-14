package com.roberto.transactions;

import java.util.List;

public class TransactionAuthorizationRequest {
    private Account account;
    private Transaction transaction;
    private List<Transaction> lastTransactions;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public List<Transaction> getLastTransactions() {
        return lastTransactions;
    }

    public void setLastTransactions(List<Transaction> lastTransactions) {
        this.lastTransactions = lastTransactions;
    }

}
