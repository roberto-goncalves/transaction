package com.roberto.transactions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import org.springframework.test.web.servlet.MockMvc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TransactionsApplicationTests {

	Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.setLenient()
			.setPrettyPrinting().create();
	String pattern = "yyyy-MM-dd HH:mm:ss";
	DateFormat df = new SimpleDateFormat(pattern);

	@Autowired
	private MockMvc mvc;

	/**
	 * Tests rule: The transaction amount should not be above limit
	 * @throws Exception
	 */
	@Test
	public void testTransactionsAmountAboveLimit() throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		Account account = new Account(true, 500, null, true);
		Transaction actualTransaction = new Transaction("Mack Grill", 150, df.parse("2018-11-17 00:00:00"));
		Transaction previousTransaction1 = new Transaction("Pão de Açucar", 200, df.parse("2018-11-17 00:00:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 200, df.parse("2018-11-17 00:00:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
			this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
					.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['Transactions amount is higher than Account limit']}"))
					.andDo(print());
	}

	/**
	 * Tests rule: No transaction should be approved when the card is blocked
	 * @throws Exception
	 */
	@Test
	public void testBlockedCard() throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		Account account = new Account(true, 500, null, false);
		Transaction actualTransaction = new Transaction("Mack Grill", 150, df.parse("2018-11-17 00:00:00"));
		Transaction previousTransaction1 = new Transaction("Pão de Açucar", 100, df.parse("2018-11-17 00:00:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 100, df.parse("2018-11-17 00:00:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['Card is blocked, cannot approve actual transaction']}"))
				.andDo(print());
	}
	/**
	 * Tests rule: Inactive card (new implemented)
	 * @throws Exception
	 */
	@Test
	public void testInactiveCard() throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		Account account = new Account(false, 500, null, true);
		Transaction actualTransaction = new Transaction("Mack Grill", 150, df.parse("2018-11-17 00:00:00"));
		Transaction previousTransaction1 = new Transaction("Pão de Açucar", 100, df.parse("2018-11-17 00:00:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 100, df.parse("2018-11-17 00:00:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['Card is inactive, cannot approve actual transaction']}"))
				.andDo(print());
	}
	/**
	 * Tests rule: The first transaction shouldn't be above 90% of the limit
	 * @throws Exception
	 */
	@Test
	public void testFirstTransactionAboveNinetyPercent () throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		Account account = new Account(true, 500, null, true);
		Transaction actualTransaction = new Transaction("Mack Grill", 4.02, df.parse("2017-11-18 00:00:00"));
		Transaction previousTransaction1 = new Transaction("Pão de Açucar", 5.98, df.parse("2016-11-17 00:00:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 451, df.parse("2015-11-15 00:00:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['First transaction is above 90% of Account limit']}"))
				.andDo(print());
	}
	/**
	 * Tests rule: There should not be more than 10 transactions on the same merchant
	 * @throws Exception
	 */
	@Test
	public void testLimitOnTenTransactionsByMerchant () throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		Account account = new Account(true, 500, null, true);
		Transaction actualTransaction = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:00:00"));
		Transaction previousTransaction1 = new Transaction("Pão de Açucar", 10, df.parse("2016-11-17 00:00:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 10, df.parse("2015-11-15 00:00:00"));
		Transaction previousTransaction3 = new Transaction("Dia", 10, df.parse("2014-11-15 00:00:00"));
		Transaction previousTransaction4 = new Transaction("Dia", 10, df.parse("2013-11-15 00:00:00"));
		Transaction previousTransaction5 = new Transaction("Dia", 10, df.parse("2012-11-15 00:00:00"));
		Transaction previousTransaction6 = new Transaction("Dia", 10, df.parse("2011-11-15 00:00:00"));
		Transaction previousTransaction7 = new Transaction("Dia", 10, df.parse("2010-11-15 00:00:00"));
		Transaction previousTransaction8 = new Transaction("Dia", 10, df.parse("2009-11-15 00:00:00"));
		Transaction previousTransaction9 = new Transaction("Dia", 10, df.parse("2008-11-15 00:00:00"));
		Transaction previousTransaction10 = new Transaction("Dia", 10, df.parse("2007-11-15 00:00:00"));
		Transaction previousTransaction11 = new Transaction("Dia", 10, df.parse("2006-11-15 00:00:00"));
		Transaction previousTransaction12 = new Transaction("Dia", 10, df.parse("2005-11-15 00:00:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		lastTransactions.add(previousTransaction3);
		lastTransactions.add(previousTransaction4);
		lastTransactions.add(previousTransaction5);
		lastTransactions.add(previousTransaction6);
		lastTransactions.add(previousTransaction7);
		lastTransactions.add(previousTransaction8);
		lastTransactions.add(previousTransaction9);
		lastTransactions.add(previousTransaction10);
		lastTransactions.add(previousTransaction11);
		lastTransactions.add(previousTransaction12);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['Transaction approved more than 10 times on merchant Dia number of times: 11']}"))
				.andDo(print());
	}
	/**
	 * Tests rule: Merchant blacklist
	 * @throws Exception
	 */
	@Test
	public void testMerchantBlackList () throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		List<String> merchantBlack = new ArrayList<>();
		merchantBlack.add("Pão de Açucar");
		merchantBlack.add("Holy Burger");
		Account account = new Account(true, 500, merchantBlack, true);
		Transaction actualTransaction = new Transaction("Dia", 10, df.parse("2017-11-18 00:00:00"));
		Transaction previousTransaction1 = new Transaction("Mack Grill", 10, df.parse("2016-11-17 00:00:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 10, df.parse("2015-11-15 00:00:00"));
		Transaction previousTransaction3 = new Transaction("Dia", 10, df.parse("2014-11-15 00:00:00"));
		Transaction previousTransaction4 = new Transaction("Mack Grill", 10, df.parse("2013-11-15 00:00:00"));
		Transaction previousTransaction5 = new Transaction("Holy Burger", 10, df.parse("2012-11-15 00:00:00"));
		Transaction previousTransaction6 = new Transaction("Mack Grill", 10, df.parse("2011-11-15 00:00:00"));
		Transaction previousTransaction7 = new Transaction("Dia", 10, df.parse("2010-11-15 00:00:00"));
		Transaction previousTransaction8 = new Transaction("Mack Grill", 10, df.parse("2009-11-15 00:00:00"));
		Transaction previousTransaction9 = new Transaction("Dia", 10, df.parse("2008-11-15 00:00:00"));
		Transaction previousTransaction10 = new Transaction("Dia", 10, df.parse("2007-11-15 00:00:00"));
		Transaction previousTransaction11 = new Transaction("Mack Grill", 10, df.parse("2006-11-15 00:00:00"));
		Transaction previousTransaction12 = new Transaction("Dia", 10, df.parse("2005-11-15 00:00:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		lastTransactions.add(previousTransaction3);
		lastTransactions.add(previousTransaction4);
		lastTransactions.add(previousTransaction5);
		lastTransactions.add(previousTransaction6);
		lastTransactions.add(previousTransaction7);
		lastTransactions.add(previousTransaction8);
		lastTransactions.add(previousTransaction9);
		lastTransactions.add(previousTransaction10);
		lastTransactions.add(previousTransaction11);
		lastTransactions.add(previousTransaction12);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['Transaction denied because account have the merchant Holy Burger on blacklist']}"))
				.andDo(print());
	}
	/**
	 * Tests rule: There should not be more than 3 transactions on a 2 minutes interval
	 * @throws Exception
	 */
	@Test
	public void testThreeTransactionsOnTwoMinutesInterval () throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		Account account = new Account(true, 500, null, true);
		Transaction actualTransaction = new Transaction("Dia", 10, df.parse("2017-11-18 00:26:55"));
		Transaction previousTransaction1 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:26:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 10, df.parse("2017-11-18 00:22:00"));
		Transaction previousTransaction3 = new Transaction("Dia", 10, df.parse("2017-11-18 00:18:00"));
		Transaction previousTransaction4 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:14:00"));
		Transaction previousTransaction5 = new Transaction("Holy Burger", 10, df.parse("2017-11-18 00:10:00"));
		Transaction previousTransaction6 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:08:00"));
		Transaction previousTransaction7 = new Transaction("Dia", 10, df.parse("2017-11-18 00:02:00"));
		Transaction previousTransaction8 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:01:00"));
		Transaction previousTransaction9 = new Transaction("Dia", 10, df.parse("2017-11-18 00:00:00"));
		Transaction previousTransaction10 = new Transaction("Dia", 10, df.parse("2017-11-17 23:50:00"));
		Transaction previousTransaction11 = new Transaction("Mack Grill", 10, df.parse("2017-11-17 23:40:00"));
		Transaction previousTransaction12 = new Transaction("Dia", 10, df.parse("2017-11-17 23:30:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		lastTransactions.add(previousTransaction3);
		lastTransactions.add(previousTransaction4);
		lastTransactions.add(previousTransaction5);
		lastTransactions.add(previousTransaction6);
		lastTransactions.add(previousTransaction7);
		lastTransactions.add(previousTransaction8);
		lastTransactions.add(previousTransaction9);
		lastTransactions.add(previousTransaction10);
		lastTransactions.add(previousTransaction11);
		lastTransactions.add(previousTransaction12);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['Transaction denied because account have more than 3 transactions on a 2 minutes interval']}"))
				.andDo(print());
	}
	/**
	 * Tests all rules
	 * @throws Exception
	 */
	@Test
	public void testAllDeniedReasons () throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		List<String> merchantBlack = new ArrayList<>();
		merchantBlack.add("Pão de Açucar");
		merchantBlack.add("Holy Burger");
		Account account = new Account(false, 500, merchantBlack, false);
		Transaction actualTransaction = new Transaction("Dia", 600, df.parse("2017-11-18 00:26:55"));
		Transaction previousTransaction1 = new Transaction("Dia", 10, df.parse("2017-11-18 00:26:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 10, df.parse("2017-11-18 00:22:00"));
		Transaction previousTransaction3 = new Transaction("Dia", 10, df.parse("2017-11-18 00:18:00"));
		Transaction previousTransaction4 = new Transaction("Dia", 10, df.parse("2017-11-18 00:14:00"));
		Transaction previousTransaction5 = new Transaction("Holy Burger", 10, df.parse("2017-11-18 00:10:00"));
		Transaction previousTransaction6 = new Transaction("Dia", 10, df.parse("2017-11-18 00:08:00"));
		Transaction previousTransaction7 = new Transaction("Dia", 10, df.parse("2017-11-18 00:02:00"));
		Transaction previousTransaction8 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:01:00"));
		Transaction previousTransaction9 = new Transaction("Dia", 10, df.parse("2017-11-18 00:00:00"));
		Transaction previousTransaction10 = new Transaction("Dia", 10, df.parse("2017-11-17 23:50:00"));
		Transaction previousTransaction11 = new Transaction("Dia", 10, df.parse("2017-11-17 23:40:00"));
		Transaction previousTransaction12 = new Transaction("Dia", 451, df.parse("2017-11-17 23:30:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		lastTransactions.add(previousTransaction3);
		lastTransactions.add(previousTransaction4);
		lastTransactions.add(previousTransaction5);
		lastTransactions.add(previousTransaction6);
		lastTransactions.add(previousTransaction7);
		lastTransactions.add(previousTransaction8);
		lastTransactions.add(previousTransaction9);
		lastTransactions.add(previousTransaction10);
		lastTransactions.add(previousTransaction11);
		lastTransactions.add(previousTransaction12);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':false,'newlimit':500.0,'deniedReasons':['Transactions amount is higher than Account limit','Card is blocked, cannot approve actual transaction','Card is inactive, cannot approve actual transaction','First transaction is above 90% of Account limit','Transaction approved more than 10 times on merchant Dia number of times: 11','Transaction denied because account have the merchant Holy Burger on blacklist','Transaction denied because account have more than 3 transactions on a 2 minutes interval']}"))
				.andDo(print());
	}
	/**
	 * Tests a authorized Transaction
	 * @throws Exception
	 */
	@Test
	public void testAuthorizedTransaction () throws Exception {
		Map<String, Object> requestMap = new HashMap<>();
		List<Transaction> lastTransactions = new ArrayList<>();
		Account account = new Account(true, 500, null, true);
		Transaction actualTransaction = new Transaction("Dia", 10, df.parse("2017-11-18 00:26:55"));
		Transaction previousTransaction1 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:26:00"));
		Transaction previousTransaction2 = new Transaction("Dia", 10, df.parse("2017-11-18 00:22:00"));
		Transaction previousTransaction3 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:18:00"));
		Transaction previousTransaction4 = new Transaction("Dia", 10, df.parse("2017-11-18 00:14:00"));
		Transaction previousTransaction5 = new Transaction("Holy Burger", 10, df.parse("2017-11-18 00:10:00"));
		Transaction previousTransaction6 = new Transaction("Dia", 10, df.parse("2017-11-18 00:09:00"));
		Transaction previousTransaction7 = new Transaction("Dia", 10, df.parse("2017-11-18 00:06:00"));
		Transaction previousTransaction8 = new Transaction("Mack Grill", 10, df.parse("2017-11-18 00:03:00"));
		Transaction previousTransaction9 = new Transaction("Dia", 10, df.parse("2017-11-18 00:00:00"));
		Transaction previousTransaction10 = new Transaction("Mack Grill", 10, df.parse("2017-11-17 23:50:00"));
		Transaction previousTransaction11 = new Transaction("Dia", 10, df.parse("2017-11-17 23:40:00"));
		Transaction previousTransaction12 = new Transaction("Mack Grill", 10, df.parse("2017-11-17 23:30:00"));
		lastTransactions.add(previousTransaction1);
		lastTransactions.add(previousTransaction2);
		lastTransactions.add(previousTransaction3);
		lastTransactions.add(previousTransaction4);
		lastTransactions.add(previousTransaction5);
		lastTransactions.add(previousTransaction6);
		lastTransactions.add(previousTransaction7);
		lastTransactions.add(previousTransaction8);
		lastTransactions.add(previousTransaction9);
		lastTransactions.add(previousTransaction10);
		lastTransactions.add(previousTransaction11);
		lastTransactions.add(previousTransaction12);
		requestMap.put("account", account);
		requestMap.put("transaction", actualTransaction);
		requestMap.put("lastTransactions", lastTransactions);
		String json = gson.toJson(requestMap);
		this.mvc.perform(post("/authorizeTransaction").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(content().json("{'approved':true,'newlimit':370.0,'deniedReasons':[]}"))
				.andDo(print());
	}
}

