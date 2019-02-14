package com.roberto.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableAutoConfiguration
@EnableScheduling
public class TransactionsApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(TransactionsApplication.class);
		app.run(args);
	}

}

