package com.financeMonkey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinanceMonkeyApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceMonkeyApplication.class, args);
    }
}
