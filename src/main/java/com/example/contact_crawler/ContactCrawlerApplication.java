package com.example.contact_crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContactCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContactCrawlerApplication.class, args);
	}

}
