package com.example.schoolchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SchoolChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchoolChatApplication.class, args);
    }
}
