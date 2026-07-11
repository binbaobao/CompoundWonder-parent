package com.compoundwonder.backtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BacktestAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BacktestAppApplication.class, args);
    }

}
