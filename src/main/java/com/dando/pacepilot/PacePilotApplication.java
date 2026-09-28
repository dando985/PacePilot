package com.dando.pacepilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PacePilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PacePilotApplication.class, args);
    }

}
