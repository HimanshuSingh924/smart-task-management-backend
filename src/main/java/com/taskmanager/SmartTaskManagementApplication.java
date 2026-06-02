package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Main entry point for the Smart Task Management System.
 * JWT security is auto-configured via the jwt-auth-starter dependency.
 */
@SpringBootApplication
public class SmartTaskManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTaskManagementApplication.class, args);
    }
}
