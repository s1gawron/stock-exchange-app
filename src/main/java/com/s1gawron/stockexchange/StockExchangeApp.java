package com.s1gawron.stockexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Clock;

@SpringBootApplication
@EnableWebMvc
@EnableCaching
@EnableScheduling
public class StockExchangeApp extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(StockExchangeApp.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}