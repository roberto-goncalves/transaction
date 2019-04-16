package com.roberto.transactions;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class TransactionHelper {

    /** Authorizes a new transaction from a request following TransactionAuthorizationRequest class
     * @param request
     * @return OutputReturn
     * @author Roberto Gonçalves
     */
    public OutputReturn authorize(TransactionAuthorizationRequest request){
        List<String> deniedReasons = new ArrayList<>();
        OutputReturn outputReturn = new OutputReturn(true, new BigDecimal(0.0), deniedReasons);
        request.getLastTransactions().add(request.getTransaction());
        orderTransactions(request.getLastTransactions());
        if(request.getAccount().getLimit().compareTo(processTransactionsAmount(request.getLastTransactions())) < 0){
            deniedReasons.add("Transactions amount is higher than Account limit");
        }
        if(!request.getAccount().isWhiteListed()){
            deniedReasons.add("Card is blocked, cannot approve actual transaction");
        }
        if(!request.getAccount().isCardActive()){
            deniedReasons.add("Card is inactive, cannot approve actual transaction");
        }
        if(request.getLastTransactions().get(0).getAmount().compareTo(request.getAccount().getLimit().multiply(new BigDecimal(0.9))) > 0){
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
            outputReturn.setNewlimit(request.getAccount().getLimit().subtract(processTransactionsAmount(request.getLastTransactions())));
        }
        return outputReturn;
    }

    /** Process amount of transactions based on the Last Transactions
     * @param lastTransactions
     * @return amount
     */
    private BigDecimal processTransactionsAmount(List<Transaction> lastTransactions){
        Iterator<Transaction> transactionIterator = lastTransactions.iterator();
        BigDecimal amount = BigDecimal.ZERO;
        int i = 0;
        while (transactionIterator.hasNext()) {
            amount = amount.add(transactionIterator.next().getAmount());
        }
        return amount;
    }

    /** Order transactions in a ascending order by date
     * @param lastTransactions
     * @return lastTransactions ordered
     */
    private List<Transaction> orderTransactions(List<Transaction> lastTransactions){
        lastTransactions.sort(Comparator.comparing(Transaction::getTime));
        return lastTransactions;
    }

    /** Return a tuple indicating wherever a merchant have more than 10 transactions
     * @param lastTransactions
     * @return
     */
    private Map.Entry<String, Integer> analyzeTransactionByMerchant(List<Transaction> lastTransactions){
        HashMap<String, Integer> transactionMerchant = countTransactionByMerchant(lastTransactions);
        Map.Entry<String, Integer> returnTuple = getTransactionByMerchantAboveTen(transactionMerchant);
        return returnTuple;
    }

    /** Count the number of transactions by merchant
     * @param lastTransactions
     * @return transactionMerchant HashMap<String, Integer>
     */
    private HashMap<String, Integer> countTransactionByMerchant(List<Transaction> lastTransactions){
        HashMap<String, Integer> transactionMerchant = new HashMap<>();
        lastTransactions.forEach((transaction) -> {
            String merchant = transaction.getMerchant();
            if(transactionMerchant.get(merchant) == null){
                transactionMerchant.put(merchant, 1);
            }else{
                int count = transactionMerchant.get(merchant);
                count++;
                transactionMerchant.put(merchant, count);
            }
        });
        return transactionMerchant;
    }

    /**
     * In a hashmap, select those who got more than 10 transactions
     * @param transactionMerchant
     * @return a pair indicating the exception or null
     */
    private Map.Entry<String, Integer> getTransactionByMerchantAboveTen(HashMap<String, Integer> transactionMerchant){
        for (Map.Entry<String, Integer> pair : transactionMerchant.entrySet()) {
            if(pair.getValue() > 10){
                return pair;
            }
        }
        return null;
    }

    /**
     * Iterates over merchantList and lastTransactions to see if it matches
     * @param merchantList
     * @param lastTransactions
     * @return the transaction who got a blacklisted merchant or null
     */
    private Transaction findMerchantBlackList(List<String> merchantList, List<Transaction> lastTransactions){
        for(String merchant : merchantList){
            for(Transaction transaction: lastTransactions){
                if(transaction.getMerchant().equals(merchant)){
                    return transaction;
                }
            }
        }
        return null;
    }

    /**
     * Iterates over lastransactions to see if it has more than 3 transactions on a 2 minutes of interval between them
     * @param lastTransactions
     * @return true or false
     */
    private boolean isThreeTransactionsOnTwoMinutes(List<Transaction> lastTransactions){
        if(countTransactionIntervalLessThanTwoMinutes(lastTransactions) >= 3){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Only counts transactions by merchant if it has 2 minutes of interval
     * @param lastTransactions
     * @return count
     * @apiNote see isThreeTransactionsOnTwoMinutes
     */
    private int countTransactionIntervalLessThanTwoMinutes(List<Transaction> lastTransactions){
        Transaction current = null;
        int count = 0;
        for(Transaction next: lastTransactions){
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

    /** Calculate minutes between dates
     * @param date1
     * @param date2
     * @return long as minutes
     */
    private long calculateMinutes(Date date1, Date date2){
        return (date1.getTime() - date2.getTime())/60000;
    }
}
