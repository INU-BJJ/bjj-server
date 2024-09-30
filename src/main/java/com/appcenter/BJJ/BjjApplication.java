package com.appcenter.BJJ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BjjApplication {
    public static void main(String[] args) {
        SpringApplication.run(BjjApplication.class, args);
    }
}
