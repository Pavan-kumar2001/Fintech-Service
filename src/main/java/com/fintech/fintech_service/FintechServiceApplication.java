package com.fintech.fintech_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class FintechServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FintechServiceApplication.class, args);
	}

}
