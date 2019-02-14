package com.roberto.transactions;

import java.util.*;

public class TransactionHelper {

    private TransactionAuthorizationRequest request;

    public TransactionHelper(TransactionAuthorizationRequest transactionAuthorizationRequest) {
        this.request = transactionAuthorizationRequest;
    }

    public Object analyze(){
        List<String> deniedReasons = new ArrayList<>();
        OutputReturn outputReturn = new OutputReturn(true, 0.0, deniedReasons);
        request.getLastTransactions().add(request.getTransaction());
        orderTransactions(request.getLastTransactions());
        if(processTransactionsAmount(request.getLastTransactions()) > request.getAccount().getLimit()){
            deniedReasons.add("Transactions amount is higher than Account limit");
        }
        if(!request.getAccount().isWhiteListed()){
            deniedReasons.add("Card is blocked, cannot approve actual transaction");
        }
        if(!request.getAccount().isCardActive()){
            deniedReasons.add("Card is inactive, cannot approve actual transaction");
        }
        if(request.getLastTransactions().get(0).getAmount() > (request.getAccount().getLimit() * 0.90)){
            deniedReasons.add("First transaction is above 90% of Account limit");
        }
        Map.Entry<String, Integer> returnTuple = analyzeTransactionByMerchant(request.getLastTransactions());
        if(returnTuple != null){
            deniedReasons.add("Transaction approved more than 10 times on merchant " + returnTuple.getKey() + " number of times: " + returnTuple.getValue());
        }
        if(request.getAccount().getBlacklist() != null){
            Transaction blackMerchantTransaction = findMerchantBlackList(request.getAccount().getBlacklist(), request.getLastTransactions());
            if(blackMerchantTransaction != null){
                deniedReasons.add("Transaction denied because account have the merchant " + blackMerchantTransaction.getMerchant() + " on blacklist");
            }
        }
        if(isThreeTransactionsOnTwoMinutes(request.getLastTransactions())){
            deniedReasons.add("Transaction denied because account have more than 3 transactions on a 2 minutes interval");
        }
        if(!deniedReasons.isEmpty()){
            outputReturn.setApproved(false);
            outputReturn.setNewlimit(request.getAccount().getLimit());
        }else{
            outputReturn.setApproved(true);
            outputReturn.setNewlimit(request.getAccount().getLimit() - processTransactionsAmount(request.getLastTransactions()));
        }
        return outputReturn;
    }

    private double processTransactionsAmount(List<Transaction> lastTransactions){
        Iterator<Transaction> transactionIterator = lastTransactions.iterator();
        double amount = 0;
        while (transactionIterator.hasNext()) {
            amount += transactionIterator.next().getAmount();
        }
        return amount;
    }

    private List<Transaction> orderTransactions(List<Transaction> lastTransactions){
        lastTransactions.sort(Comparator.comparing(Transaction::getTime));
        return lastTransactions;
    }

    private Map.Entry<String, Integer> analyzeTransactionByMerchant(List<Transaction> lastTransactions){
        HashMap<String, Integer> transactionMerchant = countTransactionByMerchant(lastTransactions);
        Map.Entry<String, Integer> returnTuple = getTransactionByMerchantAboveTen(transactionMerchant);
        return returnTuple;
    }
    private HashMap<String, Integer> countTransactionByMerchant(List<Transaction> lastTransactions){
        HashMap<String, Integer> transactionMerchant = new HashMap<>();
        Iterator<Transaction> transactionIterator = lastTransactions.iterator();
        while(transactionIterator.hasNext()){
            String merchant = transactionIterator.next().getMerchant();
            if(transactionMerchant.get(merchant) == null){
                transactionMerchant.put(merchant, 1);
            }else{
                int count = transactionMerchant.get(merchant);
                count++;
                transactionMerchant.put(merchant, count);
            }
        }
        return transactionMerchant;
    }

    private Map.Entry<String, Integer> getTransactionByMerchantAboveTen(HashMap<String, Integer> transactionMerchant){
        Iterator<Map.Entry<String, Integer>> transactionIterator = transactionMerchant.entrySet().iterator();
        while(transactionIterator.hasNext()){
            Map.Entry<String, Integer> tuple = transactionIterator.next();
            if(tuple.getValue() > 10){
                return tuple;
            }
        }
        return null;
    }

    private Transaction findMerchantBlackList(List<String> merchantList, List<Transaction> lastTransactions){
        Iterator<String> merchantIterator = merchantList.iterator();
        while(merchantIterator.hasNext()){
            String merchant = merchantIterator.next();
            Iterator<Transaction> transactionIterator = lastTransactions.iterator();
            while(transactionIterator.hasNext()){
                Transaction transaction = transactionIterator.next();
                if(transaction.getMerchant().equals(merchant)){
                    return transaction;
                }
            }
        }
        return null;
    }

    private boolean isThreeTransactionsOnTwoMinutes(List<Transaction> lastTransactions){
        if(countTransactionIntervalLessThanTwoMinutes(lastTransactions) >= 3){
            return true;
        }else{
            return false;
        }
    }
    private int countTransactionIntervalLessThanTwoMinutes(List<Transaction> lastTransactions){
        Transaction current = null;
        Transaction next;
        int count = 0;
        Iterator<Transaction> transactionIterator = lastTransactions.iterator();
        while(transactionIterator.hasNext()){
            next = transactionIterator.next();
            if(current != null){
                long minutes = calculateMinutes(next.getTime(), current.getTime());
                if(minutes < 2){
                    count++;
                }
            }
            current = next;
        }
        return count;
    }

    private long calculateMinutes(Date date1, Date date2){
        return (date1.getTime() - date2.getTime())/60000;
    }
}
