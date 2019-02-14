package com.roberto.transactions;

import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import sun.rmi.transport.tcp.TCPTransport;


import java.util.*;

@RestController
public class TransactionController {

    Gson gson = new Gson();

    @RequestMapping(path="/authorizeTransaction", method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Object authorizeTransaction(@RequestBody TransactionAuthorizationRequest request) throws Exception {
        Account account = request.getAccount();
        Transaction transaction = request.getTransaction();
        List<Transaction> lastTransactions = request.getLastTransactions();
        lastTransactions.add(transaction);
        orderTransactions(lastTransactions);
        if(processTransactionsAmount(lastTransactions) > account.getLimit()){
            return "Transactions amount is higher than Account limit";
        }
        if(!account.isWhiteListed()){
            return "Card is blocked, cannot approve actual transaction";
        }
        if(!account.isCardActive()){
            return "Card is inactive, cannot approve actual transaction";
        }
        if(lastTransactions.get(0).getAmount() > (account.getLimit() * 0.90)){
            return "First transaction is above 90% of Account limit";
        }
        Map.Entry<String, Integer> returnTuple = analyzeTransactionByMerchant(lastTransactions);
        if(returnTuple != null){
            return "Transaction approved more than 10 times on merchant " + returnTuple.getKey() + " number of times: " + returnTuple.getValue();
        }
        Transaction blackMerchantTransaction = findMerchantBlackList(account.getBlacklist(), lastTransactions);
        if(blackMerchantTransaction != null){
            return "Transaction denied because account have the merchant " + blackMerchantTransaction.getMerchant() + " on blacklist";
        }
        return lastTransactions;
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

}
