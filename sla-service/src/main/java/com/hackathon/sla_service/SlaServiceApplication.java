package com.hackathon.sla_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SlaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlaServiceApplication.class, args);
    }
}