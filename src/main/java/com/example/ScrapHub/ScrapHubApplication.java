package com.example.ScrapHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
@EnableMethodSecurity
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.example.ScrapHub.repo")
public class ScrapHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapHubApplication.class, args);
    }
}
