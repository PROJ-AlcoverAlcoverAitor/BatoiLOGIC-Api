package com.batoilogic.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BatoiLogicApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatoiLogicApplication.class, args);
    }
}
