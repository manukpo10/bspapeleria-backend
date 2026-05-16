package com.bspapeleria.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BsPapeleriaApplication {
    public static void main(String[] args) {
        SpringApplication.run(BsPapeleriaApplication.class, args);
    }
}