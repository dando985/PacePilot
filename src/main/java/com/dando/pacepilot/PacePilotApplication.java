package com.dando.pacepilot;

import com.dando.pacepilot.ollama.OllamaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OllamaProperties.class)
public class PacePilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PacePilotApplication.class, args);
    }

}
