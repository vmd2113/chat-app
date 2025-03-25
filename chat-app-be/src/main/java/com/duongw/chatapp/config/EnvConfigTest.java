package com.duongw.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfigTest {

    @Bean
    public CommandLineRunner testEnvConfig(
            @Value("${spring.datasource.username}") String dbUser,
            @Value("${jwt.secret}") String jwtSecret) {
        return args -> {
            System.out.println("==== ENV CONFIG TEST ====");
            System.out.println("Database User: " + dbUser);
            // Only print first few characters of the secret
            System.out.println("JWT Secret starts with: " +
                    (jwtSecret.length() > 10 ? jwtSecret.substring(0, 10) + "..." : jwtSecret));
        };
    }
}