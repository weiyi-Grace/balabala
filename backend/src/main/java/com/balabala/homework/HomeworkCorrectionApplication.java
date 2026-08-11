package com.balabala.homework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.balabala.homework")
@EnableCaching
public class HomeworkCorrectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeworkCorrectionApplication.class, args);
    }
}
