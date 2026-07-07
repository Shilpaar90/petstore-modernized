package com.example.petstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the modernized Java Pet Store storefront.
 *
 * <p>Replaces the legacy J2EE 1.3 multi-EAR deployment (petstore.ear + WAF servlet
 * front controller + EJB container) with a single self-contained Spring Boot process.
 */
@SpringBootApplication
public class PetstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetstoreApplication.class, args);
    }
}
