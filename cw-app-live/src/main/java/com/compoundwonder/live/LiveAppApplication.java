package com.compoundwonder.live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(
        scanBasePackages = "com.compoundwonder"
)
public class LiveAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiveAppApplication.class, args);
    }

}
