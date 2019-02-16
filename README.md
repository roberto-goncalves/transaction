# Transaction

Transaction authorization made with Spring Boot

## Requirements

* JDK 8;
* Maven 3.5;

## Tests

To run all tests go to the root folder and run

```console
$ mvn test
```
The tests are divided on single method/rule as follows:

* testTransactionsAmountAboveLimit - Tests rule: The transaction amount should not be above limit
* testBlockedCard - Tests rule: No transaction should be approved when the card is blocked
* testInactiveCard - Tests rule: Inactive card (new implemented)
* testFirstTransactionAboveNinetyPercent - Tests rule: The first transaction shouldn't be above 90% of the limit
* testLimitOnTenTransactionsByMerchant - Tests rule: There should not be more than 10 transactions on the same merchant
* testMerchantBlackList - Tests rule: Merchant blacklist
* testThreeTransactionsOnTwoMinutesInterval - Tests rule: There should not be more than 3 transactions on a 2 minutes interval
* testAllDeniedReasons - Tests all rules
* testAuthorizedTransaction - Tests a authorized Transaction

## Running application

Application runs on port 8080

```console
  $ mvn spring-boot:run
```

## To create a jar & run it

Application runs on port 8080

```console
   $ mvn package
```
```console
   $ java -jar target/transactions-0.0.1-SNAPSHOT.jar
```

### Example of a valid request
POST http://0.0.0.0:8080/authorizeTransaction
```json
   {
  "lastTransactions": [
    {
      "merchant": "Pão de Açucar",
      "amount": 100.0,
      "time": "2012-11-15T00:00:00.000-0200"
    },
    {
      "merchant": "Dia",
      "amount": 100.0,
      "time": "2012-11-15T00:00:00.000-0200"
    }
  ],
  "account": {
    "isCardActive": true,
    "limit": 500.0,
    "isWhiteListed": true
  },
  "transaction": {
    "merchant": "Mack Grill",
    "amount": 234.57,
    "time": "2012-11-15T00:00:00.000-0200"
  }
}
```
Output:
```json
{
    "approved": true,
    "newlimit": 65.43,
    "deniedReasons": []
}
```
