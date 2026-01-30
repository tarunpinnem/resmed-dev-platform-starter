package com.healthcare.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Healthcare Platform Starter Kit
 * 
 * A cloud-native, secure, and developer-friendly healthcare service template.
 * Features: JWT auth, rate limiting, structured logging, OpenAPI docs.
 */
@SpringBootApplication
public class HealthcarePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthcarePlatformApplication.class, args);
    }
}
