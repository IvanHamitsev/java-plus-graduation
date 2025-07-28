package ru.practicum.client;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatsFeignClientConfig {
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(300L, 5000L, 5);
    }
}
