package com.safetynet.safetynet_alerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point of the SafetyNet Alerts application.
 * This class initializes and starts the Spring Boot application.
 */
@SpringBootApplication
public class SafetynetAlertsApplication {

    /**
     * Launches the Spring Boot application.
     *
     * @param args Optional command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(SafetynetAlertsApplication.class, args);
    }
}
