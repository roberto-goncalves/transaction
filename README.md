# Transaction

Transaction authorization made with Spring Boot

## Requirements

* Java JDK 8;
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

This command will make application available on port 8080

```console
  $ mvn spring-boot:run
```
