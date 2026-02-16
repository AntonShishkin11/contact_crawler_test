package com.example.contact_crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService crawlExecutor(CrawlerThreadFactory threadFactory) {
        return Executors.newFixedThreadPool(4, threadFactory);
    }
}
